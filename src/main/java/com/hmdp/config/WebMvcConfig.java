package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    public WebMvcConfig(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. token 刷新：拦截所有请求，只做刷新不做拦截
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(0);

        // 2. 登录校验：排除静态资源与公开接口
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        // 静态资源
                        "/", "/*.html", "/css/**", "/js/**", "/imgs/**", "/upload/**", "/favicon.ico",
                        // 登录相关
                        "/user/code", "/user/login",
                        // 公开查询接口
                        "/shop/**", "/shop-type/**",
                        "/blog/hot", "/blog/of/shop", "/blog/*",
                        "/voucher/list/**", "/voucher/seckill/list", "/voucher/*"
                )
                .order(1);
    }

    /**
     * 把本地 upload 目录映射为 /upload/**，用于笔记图片上传
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = System.getProperty("user.dir") + File.separator + "upload" + File.separator;
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + dir);
    }

    /**
     * 允许跨域，方便用其它端口的前端调试
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
