package com.hmdp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.common.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 缓存工具：封装「缓存穿透 / 击穿 / 雪崩」三种典型问题的解决方案。
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** 逻辑过期重建线程池 */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    // ---------------------------------------------------------------- 基础写入

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, toJson(value), time, unit);
    }

    /**
     * 写入带逻辑过期时间的数据（物理不过期，靠字段判断）
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, toJson(redisData));
    }

    // ------------------------------------------------- 方案一：缓存穿透 + 互斥锁

    /**
     * 缓存穿透 + 缓存击穿（互斥锁）通用查询
     *
     * @param keyPrefix  key 前缀
     * @param id         查询 id
     * @param type       返回类型
     * @param dbFallback 数据库回查函数
     * @param time       缓存时间
     * @param unit       时间单位
     */
    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type,
                                    Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;

        // 1. 查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, type);
        }
        // 2. 命中的是空值缓存（缓存穿透防护）
        if (json != null) {
            return null;
        }

        // 3. 未命中，尝试获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        R r;
        try {
            if (!tryLock(lockKey)) {
                // 获取锁失败，短暂休眠后重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4. 双重检查：可能其它线程已经重建完成
            json = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(json)) {
                return fromJson(json, type);
            }
            if (json != null) {
                return null;
            }

            // 5. 查数据库
            r = dbFallback.apply(id);
            if (r == null) {
                // 缓存空值，TTL 较短，防止缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6. TTL 加随机值，防止缓存雪崩
            long ttl = unit.toSeconds(time) + (long) (Math.random() * 300);
            stringRedisTemplate.opsForValue().set(key, toJson(r), ttl, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            unlock(lockKey);
        }
        return r;
    }

    // ---------------------------------------------------- 方案二：缓存穿透（纯旁路）

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, type);
        }
        if (json != null) {
            return null;
        }
        R r = dbFallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        set(key, r, time, unit);
        return r;
    }

    // ------------------------------------------------- 方案三：缓存击穿（逻辑过期）

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                            Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 逻辑过期方案要求热点数据提前预热，未命中直接返回 null
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            LocalDateTime expireTime = objectMapper.convertValue(root.get("expireTime"), LocalDateTime.class);
            JsonNode dataNode = root.get("data");

            // 未过期，直接返回
            if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
                return objectMapper.convertValue(dataNode, type);
            }

            // 已过期，尝试获取锁，异步重建
            String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
            if (tryLock(lockKey)) {
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        R r = dbFallback.apply(id);
                        setWithLogicalExpire(key, r, time, unit);
                    } catch (Exception e) {
                        log.error("缓存重建失败, key={}", key, e);
                    } finally {
                        unlock(lockKey);
                    }
                });
            }
            // 无论是否拿到锁，都先返回旧数据
            return objectMapper.convertValue(dataNode, type);
        } catch (Exception e) {
            throw new RuntimeException("缓存反序列化失败", e);
        }
    }

    // ---------------------------------------------------------------- 私有方法

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    private <R> R fromJson(String json, Class<R> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
