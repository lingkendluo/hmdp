package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.FollowService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    /** Redis 中保存关注关系的 Set 前缀 */
    private static final String FOLLOW_KEY = "follow:";

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;

    public FollowServiceImpl(StringRedisTemplate stringRedisTemplate, UserMapper userMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        if (userId.equals(followUserId)) {
            return Result.fail("不能关注自己");
        }
        Long count = query()
                .eq("user_id", userId)
                .eq("follow_user_id", followUserId)
                .count();

        if (Boolean.TRUE.equals(isFollow)) {
            if (count == 0) {
                Follow follow = new Follow();
                follow.setUserId(userId);
                follow.setFollowUserId(followUserId);
                save(follow);
            }
            stringRedisTemplate.opsForSet().add(FOLLOW_KEY + userId, followUserId.toString());
        } else {
            if (count > 0) {
                remove(Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getUserId, userId)
                        .eq(Follow::getFollowUserId, followUserId));
            }
            stringRedisTemplate.opsForSet().remove(FOLLOW_KEY + userId, followUserId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.ok(false);
        }
        Long count = query()
                .eq("user_id", userId)
                .eq("follow_user_id", followUserId)
                .count();
        return Result.ok(count != null && count > 0);
    }

    @Override
    public Result followCommons(Long id) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        String key1 = FOLLOW_KEY + userId;
        String key2 = FOLLOW_KEY + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);

        List<Long> ids;
        if (intersect == null || intersect.isEmpty()) {
            // Redis 中没有数据时，退化为数据库交集
            List<Long> mine = query().eq("user_id", userId).list()
                    .stream().map(Follow::getFollowUserId).toList();
            List<Long> his = query().eq("user_id", id).list()
                    .stream().map(Follow::getFollowUserId).toList();
            ids = mine.stream().filter(his::contains).toList();
        } else {
            ids = intersect.stream().map(Long::valueOf).toList();
        }

        if (ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<User> users = userMapper.selectBatchIds(ids);
        return Result.ok(users.stream().map(this::toDTO).toList());
    }

    @Override
    public Result queryFollows(Long userId) {
        List<Long> ids = query().eq("user_id", userId).list()
                .stream().map(Follow::getFollowUserId).toList();
        if (ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<User> users = userMapper.selectBatchIds(ids);
        List<UserDTO> list = new ArrayList<>();
        for (Long id : ids) {
            users.stream().filter(u -> u.getId().equals(id)).findFirst()
                    .ifPresent(u -> list.add(toDTO(u)));
        }
        return Result.ok(list);
    }

    @Override
    public Result queryFans(Long userId) {
        List<Long> ids = query().eq("follow_user_id", userId).list()
                .stream().map(Follow::getUserId).toList();
        if (ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<User> users = userMapper.selectBatchIds(ids);
        return Result.ok(users.stream().map(this::toDTO).toList());
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNickName(user.getNickName());
        dto.setIcon(user.getIcon());
        return dto;
    }
}
