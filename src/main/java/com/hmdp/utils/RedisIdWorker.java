package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 基于 Redis 自增的全局唯一 ID 生成器。
 * <p>
 * 结构：1 位符号位（恒为 0） + 31 位时间戳（秒） + 32 位序列号
 */
@Component
public class RedisIdWorker {

    /** 起始时间戳：2024-01-01 00:00:00 UTC */
    private static final long BEGIN_TIMESTAMP = 1704067200L;

    /** 序列号位数 */
    private static final int COUNT_BITS = 32;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 1. 时间戳
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 序列号：按天做 key，避免单个 key 无限增长，也便于统计
        String date = LocalDate.now().format(DATE_FORMATTER);
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3. 拼接
        return timestamp << COUNT_BITS | count;
    }
}
