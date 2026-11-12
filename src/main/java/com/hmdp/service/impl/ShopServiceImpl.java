package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.common.SystemConstants;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.ShopService;
import com.hmdp.utils.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    private final CacheClient cacheClient;
    private final StringRedisTemplate stringRedisTemplate;

    public ShopServiceImpl(CacheClient cacheClient, StringRedisTemplate stringRedisTemplate) {
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result queryById(Long id) {
        // 互斥锁 + 空值缓存：同时解决穿透与击穿
        Shop shop = cacheClient.queryWithMutex(
                RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
                this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    public Result queryByIdWithLogicalExpire(Long id) {
        Shop shop = cacheClient.queryWithLogicalExpire(
                RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
                this::getById, RedisConstants.CACHE_SHOP_LOGICAL_TTL, TimeUnit.SECONDS);
        if (shop == null) {
            return Result.fail("店铺不存在，请先执行预热 /shop/preheat");
        }
        return Result.ok(shop);
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        int cur = (current == null || current < 1) ? 1 : current;
        int size = SystemConstants.DEFAULT_PAGE_SIZE;

        // 没有坐标，退化为普通分页
        if (x == null || y == null) {
            IPage<Shop> page = query()
                    .eq(typeId != null, "type_id", typeId)
                    .orderByDesc("score").orderByDesc("sold")
                    .page(new Page<>(cur, size));
            return Result.ok(page.getRecords(), page.getTotal());
        }

        // 有坐标：使用 Redis GEO 按距离检索
        String key = RedisConstants.SHOP_GEO_KEY + (typeId == null ? "all" : typeId);
        Circle circle = new Circle(new Point(x, y), new Distance(10, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(200);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().radius(key, circle, args);

        if (results == null || results.getContent().isEmpty()) {
            // GEO 未预热或附近没有店铺，退化为普通分页
            IPage<Shop> page = query()
                    .eq(typeId != null, "type_id", typeId)
                    .orderByDesc("score").orderByDesc("sold")
                    .page(new Page<>(cur, size));
            return Result.ok(page.getRecords(), page.getTotal());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        int total = content.size();
        int from = (cur - 1) * size;
        if (from >= total) {
            return Result.ok(Collections.emptyList(), (long) total);
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> pageList =
                content.subList(from, Math.min(from + size, total));

        List<Long> ids = new ArrayList<>(pageList.size());
        Map<Long, Double> distanceMap = new HashMap<>(pageList.size());
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : pageList) {
            Long shopId = Long.valueOf(r.getContent().getName());
            ids.add(shopId);
            distanceMap.put(shopId, r.getDistance().getValue());
        }

        Map<Long, Shop> shopMap = listByIds(ids).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s));

        List<Shop> shops = ids.stream()
                .map(shopMap::get)
                .filter(java.util.Objects::nonNull)
                .peek(s -> {
                    Double d = distanceMap.get(s.getId());
                    // 距离转为米，保留整数
                    s.setDistance(d == null ? null : Math.round(d * 1000) / 1.0);
                })
                .collect(Collectors.toList());

        return Result.ok(shops, (long) total);
    }

    @Override
    public Result queryShopByName(String name, Integer current) {
        int cur = (current == null || current < 1) ? 1 : current;
        IPage<Shop> page = query()
                .like(StringUtils.hasText(name), "name", name)
                .orderByDesc("score")
                .page(new Page<>(cur, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result queryHotShops(Integer current) {
        int cur = (current == null || current < 1) ? 1 : current;
        IPage<Shop> page = query()
                .orderByDesc("sold")
                .page(new Page<>(cur, 6));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public void saveShop2Redis(Long id, Long expireSeconds) {
        Shop shop = getById(id);
        if (shop == null) {
            return;
        }
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + id, shop,
                expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void preheatGeo() {
        List<Shop> shops = list();
        if (shops.isEmpty()) {
            return;
        }
        // 清理旧数据后重建
        for (Shop shop : shops) {
            String typeKey = RedisConstants.SHOP_GEO_KEY + shop.getTypeId();
            stringRedisTemplate.opsForGeo().add(typeKey, new Point(shop.getX(), shop.getY()), shop.getId().toString());
            stringRedisTemplate.opsForGeo().add(RedisConstants.SHOP_GEO_KEY + "all",
                    new Point(shop.getX(), shop.getY()), shop.getId().toString());
        }
        log.info("店铺 GEO 预热完成，共 {} 家", shops.size());
    }

    @Override
    public Result addShop(Shop shop) {
        if (shop.getTypeId() == null || !StringUtils.hasText(shop.getName())) {
            return Result.fail("店铺名称和类型不能为空");
        }
        if (shop.getImages() == null) {
            shop.setImages("");
        }
        save(shop);
        if (shop.getX() != null && shop.getY() != null) {
            stringRedisTemplate.opsForGeo().add(RedisConstants.SHOP_GEO_KEY + shop.getTypeId(),
                    new Point(shop.getX(), shop.getY()), shop.getId().toString());
        }
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok(shop.getId());
    }

    @Override
    public Result updateShop(Shop shop) {
        if (shop.getId() == null) {
            return Result.fail("店铺 id 不能为空");
        }
        updateById(shop);
        // 删除缓存，保证一致性
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
