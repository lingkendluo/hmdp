package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.common.SystemConstants;
import com.hmdp.dto.BlogVO;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.BlogService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public BlogServiceImpl(UserMapper userMapper, FollowMapper followMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result saveBlog(Blog blog) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        blog.setUserId(userId);
        if (blog.getLiked() == null) {
            blog.setLiked(0);
        }
        if (blog.getComments() == null) {
            blog.setComments(0);
        }
        save(blog);

        // 推送到粉丝的收件箱（Feed 流，推模式）
        List<Follow> followers = followMapper.selectList(
                Wrappers.<Follow>lambdaQuery().eq(Follow::getFollowUserId, userId));
        for (Follow follow : followers) {
            String key = RedisConstants.FEED_KEY + follow.getUserId();
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryHotBlog(Integer current) {
        int cur = (current == null || current < 1) ? 1 : current;
        IPage<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(cur, SystemConstants.DEFAULT_PAGE_SIZE));
        List<BlogVO> vos = page.getRecords().stream().map(this::toVO).toList();
        vos.forEach(this::fillLiked);
        return Result.ok(vos, page.getTotal());
    }

    @Override
    public Result queryMyBlog(Integer current) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return queryBlogOfUser(userId, current);
    }

    @Override
    public Result queryBlogOfShop(Long shopId, Integer current) {
        int cur = (current == null || current < 1) ? 1 : current;
        IPage<BlogVO> page = baseMapper.pageByShopId(new Page<>(cur, SystemConstants.DEFAULT_PAGE_SIZE), shopId);
        page.getRecords().forEach(this::fillLiked);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result queryBlogOfUser(Long userId, Integer current) {
        int cur = (current == null || current < 1) ? 1 : current;
        IPage<BlogVO> page = baseMapper.pageByUserId(new Page<>(cur, SystemConstants.DEFAULT_PAGE_SIZE), userId);
        page.getRecords().forEach(this::fillLiked);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result queryBlogById(Long id) {
        BlogVO vo = baseMapper.getVOById(id);
        if (vo == null) {
            return Result.fail("笔记不存在");
        }
        fillLiked(vo);
        return Result.ok(vo);
    }

    @Override
    public Result likeBlog(Long id) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        boolean liked;
        if (score == null) {
            // 未点赞 -> 点赞
            boolean success = update(Wrappers.<Blog>lambdaUpdate()
                    .setSql("liked = liked + 1")
                    .eq(Blog::getId, id));
            if (success) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
            liked = true;
        } else {
            // 已点赞 -> 取消
            boolean success = update(Wrappers.<Blog>lambdaUpdate()
                    .setSql("liked = liked - 1")
                    .eq(Blog::getId, id)
                    .gt(Blog::getLiked, 0));
            if (success) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
            liked = false;
        }
        Blog blog = getById(id);
        java.util.Map<String, Object> data = new java.util.HashMap<>(4);
        data.put("liked", liked);
        data.put("likedCount", blog == null ? 0 : blog.getLiked());
        return Result.ok(data);
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Set<String> ids = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (ids == null || ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> userIds = ids.stream().map(Long::valueOf).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        List<UserDTO> dtos = new ArrayList<>();
        // 按点赞时间顺序返回
        for (Long uid : userIds) {
            users.stream().filter(u -> u.getId().equals(uid)).findFirst().ifPresent(u -> {
                UserDTO dto = new UserDTO();
                dto.setId(u.getId());
                dto.setNickName(u.getNickName());
                dto.setIcon(u.getIcon());
                dtos.add(dto);
            });
        }
        return Result.ok(dtos);
    }

    @Override
    public Result queryBlogOfFollow(Long maxTime, Integer offset) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        String key = RedisConstants.FEED_KEY + userId;
        long max = maxTime == null ? System.currentTimeMillis() : maxTime;
        int os = offset == null ? 0 : offset;

        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, os, 2);

        ScrollResult result = new ScrollResult();
        if (tuples == null || tuples.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setMinTime(0L);
            result.setOffset(0);
            return Result.ok(result);
        }

        List<Long> ids = new ArrayList<>(tuples.size());
        long minTime = 0;
        int newOffset = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore() == null ? 0 : tuple.getScore().longValue();
            if (time == minTime) {
                newOffset++;
            } else {
                minTime = time;
                newOffset = 1;
            }
        }

        List<BlogVO> vos = baseMapper.listVOByIds(ids);
        vos.forEach(this::fillLiked);
        // 保持按时间倒序
        List<BlogVO> ordered = new ArrayList<>(vos.size());
        for (Long id : ids) {
            vos.stream().filter(v -> v.getId().equals(id)).findFirst().ifPresent(ordered::add);
        }

        result.setList(ordered);
        result.setMinTime(minTime);
        result.setOffset(newOffset);
        return Result.ok(result);
    }

    // ------------------------------------------------------------- 私有方法

    private BlogVO toVO(Blog blog) {
        BlogVO vo = new BlogVO();
        org.springframework.beans.BeanUtils.copyProperties(blog, vo);
        User user = userMapper.selectById(blog.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickName());
            vo.setUserIcon(user.getIcon());
        }
        return vo;
    }

    private void fillLiked(BlogVO vo) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            vo.setIsLiked(false);
            return;
        }
        Double score = stringRedisTemplate.opsForZSet()
                .score(RedisConstants.BLOG_LIKED_KEY + vo.getId(), userId.toString());
        vo.setIsLiked(score != null);
    }
}
