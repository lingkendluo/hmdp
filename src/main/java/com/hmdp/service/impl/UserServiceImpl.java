package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.common.SystemConstants;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.UserInfoMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.UserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserInfoMapper userInfoMapper;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate, UserInfoMapper userInfoMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userInfoMapper = userInfoMapper;
    }

    @Override
    public Result sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 生成 6 位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        // 保存到 Redis，2 分钟有效
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.info("发送短信验证码，phone={}, code={}", phone, code);

        // 真实环境应调用短信服务，这里直接把验证码返回给前端，便于演示
        Map<String, Object> data = new HashMap<>(4);
        data.put("code", code);
        data.put("expireSeconds", TimeUnit.MINUTES.toSeconds(RedisConstants.LOGIN_CODE_TTL));
        return Result.ok(data);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误或已过期");
        }

        // 校验通过，删除验证码
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);

        // 查询用户，不存在则注册
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        // 生成 token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 用户信息以 Hash 结构存入 Redis
        Map<String, String> userMap = new HashMap<>(4);
        userMap.put("id", user.getId().toString());
        userMap.put("nickName", user.getNickName());
        userMap.put("icon", user.getIcon());
        String key = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        Map<String, Object> data = new HashMap<>(4);
        data.put("token", token);
        data.put("user", user);
        return Result.ok(data);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 8));
        int avatar = (int) (Math.random() * 20) + 1;
        user.setIcon("/imgs/avatar/" + avatar + ".svg");
        save(user);

        UserInfo info = new UserInfo();
        info.setUserId(user.getId());
        info.setCity("北京");
        info.setIntroduce("这个人很懒，什么都没写");
        info.setGender(0);
        info.setCredits(0);
        info.setLevel(1);
        userInfoMapper.insert(info);
        return user;
    }

    @Override
    public Result logout(String token) {
        if (StringUtils.hasText(token)) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + token);
        }
        return Result.ok();
    }

    @Override
    public Result me() {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("未登录");
        }
        return queryUserById(current.getId());
    }

    @Override
    public Result queryUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        UserInfo info = userInfoMapper.selectById(id);
        Map<String, Object> data = new HashMap<>(8);
        data.put("user", user);
        data.put("info", info);
        return Result.ok(data);
    }

    @Override
    public Result updateUserInfo(UserInfo userInfo) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("未登录");
        }
        userInfo.setUserId(userId);
        if (userInfoMapper.selectById(userId) == null) {
            userInfo.setCreateTime(LocalDateTime.now());
            userInfoMapper.insert(userInfo);
        } else {
            userInfoMapper.updateById(userInfo);
        }
        return Result.ok();
    }
}
