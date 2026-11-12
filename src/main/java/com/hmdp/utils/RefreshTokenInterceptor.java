package com.hmdp.utils;

import com.hmdp.common.RedisConstants;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 第一层拦截器：刷新 token 有效期，并把用户信息放入 ThreadLocal。
 * 无论是否登录都会放行，由 LoginInterceptor 决定是否拦截。
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("authorization");
        if (!StringUtils.hasText(token)) {
            return true;
        }
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            return true;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(Long.valueOf(userMap.get("id").toString()));
        Object nickName = userMap.get("nickName");
        Object icon = userMap.get("icon");
        userDTO.setNickName(nickName == null ? "" : nickName.toString());
        userDTO.setIcon(icon == null ? "" : icon.toString());
        UserHolder.saveUser(userDTO);

        // 刷新有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
