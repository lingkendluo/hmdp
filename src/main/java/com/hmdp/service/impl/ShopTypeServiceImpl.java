package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.ShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    private static final String KEY = RedisConstants.CACHE_SHOP_TYPE_KEY + "list";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ShopTypeServiceImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Result queryList() {
        try {
            String json = stringRedisTemplate.opsForValue().get(KEY);
            if (StringUtils.hasText(json)) {
                List<ShopType> cached = objectMapper.readValue(json, new TypeReference<List<ShopType>>() {
                });
                return Result.ok(cached);
            }
            List<ShopType> list = query().orderByAsc("sort").list();
            stringRedisTemplate.opsForValue().set(KEY, objectMapper.writeValueAsString(list),
                    RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询商户分类失败", e);
            return Result.fail("查询商户分类失败");
        }
    }
}
