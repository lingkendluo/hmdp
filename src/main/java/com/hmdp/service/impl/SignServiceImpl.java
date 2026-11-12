package com.hmdp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.entity.Sign;
import com.hmdp.mapper.SignMapper;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户签到：Redis BitMap，每月一个 key，bit 下标 = 日期 - 1
 */
@Slf4j
@Service
public class SignServiceImpl implements SignService {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final StringRedisTemplate stringRedisTemplate;
    private final SignMapper signMapper;

    public SignServiceImpl(StringRedisTemplate stringRedisTemplate, SignMapper signMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.signMapper = signMapper;
    }

    private String key(Long userId, LocalDate date) {
        return RedisConstants.USER_SIGN_KEY + userId + ":" + date.format(MONTH_FORMAT);
    }

    @Override
    public Result sign() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        LocalDate now = LocalDate.now();
        String key = key(userId, now);
        int day = now.getDayOfMonth();

        Boolean old = stringRedisTemplate.opsForValue().getBit(key, day - 1);
        if (Boolean.TRUE.equals(old)) {
            return Result.fail("今天已经签到过了");
        }

        stringRedisTemplate.opsForValue().setBit(key, day - 1, true);
        // 当月 key 保留 3 个月
        stringRedisTemplate.expire(key, java.time.Duration.ofDays(90));

        // 落库，便于统计与补签
        Long count = signMapper.selectCount(Wrappers.<Sign>lambdaQuery()
                .eq(Sign::getUserId, userId)
                .eq(Sign::getDate, now));
        if (count == null || count == 0) {
            Sign sign = new Sign();
            sign.setUserId(userId);
            sign.setYear(now.getYear());
            sign.setMonth(now.getMonthValue());
            sign.setDate(now);
            sign.setIsBackup(0);
            signMapper.insert(sign);
        }

        Map<String, Object> data = new HashMap<>(4);
        data.put("continuousDays", continuousDays(userId, now));
        data.put("todaySigned", true);
        return Result.ok(data);
    }

    @Override
    public Result signCount() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        LocalDate now = LocalDate.now();
        int continuous = continuousDays(userId, now);
        int total = totalDays(userId, now);

        Map<String, Object> data = new HashMap<>(4);
        data.put("continuousDays", continuous);
        data.put("totalDays", total);
        return Result.ok(data);
    }

    @Override
    public Result signStatus() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        LocalDate now = LocalDate.now();
        String key = key(userId, now);
        int today = now.getDayOfMonth();

        List<Integer> signedDays = new ArrayList<>();
        for (int i = 0; i < today; i++) {
            if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().getBit(key, i))) {
                signedDays.add(i + 1);
            }
        }

        Map<String, Object> data = new HashMap<>(8);
        data.put("year", now.getYear());
        data.put("month", now.getMonthValue());
        data.put("today", today);
        data.put("signedDays", signedDays);
        data.put("continuousDays", continuousDays(userId, now));
        data.put("todaySigned", signedDays.contains(today));
        return Result.ok(data);
    }

    /**
     * 从今天往前推，连续签到了多少天
     */
    private int continuousDays(Long userId, LocalDate date) {
        String key = key(userId, date);
        int day = date.getDayOfMonth();
        int count = 0;
        for (int i = day; i >= 1; i--) {
            Boolean bit = stringRedisTemplate.opsForValue().getBit(key, i - 1);
            if (Boolean.TRUE.equals(bit)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 本月累计签到天数
     */
    private int totalDays(Long userId, LocalDate date) {
        String key = key(userId, date);
        int day = date.getDayOfMonth();
        int count = 0;
        for (int i = 0; i < day; i++) {
            Boolean bit = stringRedisTemplate.opsForValue().getBit(key, i);
            if (Boolean.TRUE.equals(bit)) {
                count++;
            }
        }
        return count;
    }
}
