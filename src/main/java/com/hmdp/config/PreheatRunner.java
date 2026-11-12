package com.hmdp.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hmdp.common.RedisConstants;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.ShopService;
import com.hmdp.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

/**
 * 启动预热：
 * 1. 店铺坐标写入 Redis GEO
 * 2. 秒杀券库存写入 Redis
 * 3. 关注流 Feed 预热（把已关注博主的历史笔记写入收件箱）
 */
@Slf4j
@Component
public class PreheatRunner implements ApplicationRunner {

    private final ShopService shopService;
    private final VoucherService voucherService;
    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final BlogMapper blogMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public PreheatRunner(ShopService shopService, VoucherService voucherService,
                         UserMapper userMapper, FollowMapper followMapper,
                         BlogMapper blogMapper, StringRedisTemplate stringRedisTemplate) {
        this.shopService = shopService;
        this.voucherService = voucherService;
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.blogMapper = blogMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            shopService.preheatGeo();
        } catch (Exception e) {
            log.warn("GEO 预热失败：{}", e.getMessage());
        }
        try {
            voucherService.preheatSeckillStock();
        } catch (Exception e) {
            log.warn("秒杀库存预热失败：{}", e.getMessage());
        }
        try {
            preheatFeed();
        } catch (Exception e) {
            log.warn("Feed 预热失败：{}", e.getMessage());
        }
    }

    /**
     * 演示数据里关注关系是预置的，但历史笔记不会触发推送。
     * 这里把「已关注博主的历史笔记」补写进 Feed，避免关注页空白。
     */
    private void preheatFeed() {
        List<User> users = userMapper.selectList(null);
        int total = 0;
        for (User user : users) {
            String key = RedisConstants.FEED_KEY + user.getId();
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
                continue;
            }
            List<Follow> follows = followMapper.selectList(
                    Wrappers.<Follow>lambdaQuery().eq(Follow::getUserId, user.getId()));
            if (follows.isEmpty()) {
                continue;
            }
            List<Long> bloggerIds = follows.stream().map(Follow::getFollowUserId).toList();
            List<Blog> blogs = blogMapper.selectList(Wrappers.<Blog>lambdaQuery()
                    .in(Blog::getUserId, bloggerIds)
                    .orderByDesc(Blog::getCreateTime)
                    .last("limit 30"));
            for (Blog blog : blogs) {
                long ts = blog.getCreateTime() == null
                        ? System.currentTimeMillis()
                        : blog.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), ts);
                total++;
            }
        }
        log.info("关注流 Feed 预热完成，共写入 {} 条", total);
    }
}
