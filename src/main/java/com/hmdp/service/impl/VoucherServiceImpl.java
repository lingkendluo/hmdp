package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.mapper.VoucherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VoucherServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<VoucherMapper, Voucher>
        implements VoucherService {

    private final SeckillVoucherMapper seckillVoucherMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public VoucherServiceImpl(SeckillVoucherMapper seckillVoucherMapper, StringRedisTemplate stringRedisTemplate) {
        this.seckillVoucherMapper = seckillVoucherMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        List<Voucher> list = baseMapper.listByShopId(shopId);
        return Result.ok(list);
    }

    @Override
    public Result querySeckillList() {
        return Result.ok(baseMapper.listSeckill());
    }

    @Override
    public Result queryVoucherById(Long id) {
        Voucher voucher = baseMapper.getSeckillById(id);
        if (voucher == null) {
            voucher = getById(id);
        }
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }
        // 库存以 Redis 为准，保证与秒杀实时一致
        String stock = stringRedisTemplate.opsForValue().get(RedisConstants.SECKILL_STOCK_KEY + id);
        if (stock != null) {
            voucher.setStock(Integer.valueOf(stock));
        }
        return Result.ok(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addSeckillVoucher(Voucher voucher) {
        if (voucher.getType() == null) {
            voucher.setType(1);
        }
        if (voucher.getStatus() == null) {
            voucher.setStatus(1);
        }
        save(voucher);

        SeckillVoucher sv = new SeckillVoucher();
        sv.setVoucherId(voucher.getId());
        sv.setStock(voucher.getStock() == null ? 0 : voucher.getStock());
        sv.setBeginTime(voucher.getBeginTime());
        sv.setEndTime(voucher.getEndTime());
        seckillVoucherMapper.insert(sv);

        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(),
                sv.getStock().toString());
        return Result.ok(voucher.getId());
    }

    @Override
    public void preheatSeckillStock() {
        List<Voucher> list = baseMapper.listSeckill();
        for (Voucher v : list) {
            String key = RedisConstants.SECKILL_STOCK_KEY + v.getId();
            // setIfAbsent：重启时不覆盖已扣减的库存
            Boolean absent = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, String.valueOf(v.getStock()), 7, TimeUnit.DAYS);
            if (Boolean.TRUE.equals(absent)) {
                log.info("秒杀库存预热：voucherId={}, stock={}", v.getId(), v.getStock());
            }
        }
    }

    @Override
    public Result resetStock() {
        List<Voucher> list = baseMapper.listSeckill();
        for (Voucher v : list) {
            stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + v.getId(),
                    String.valueOf(v.getStock()));
            stringRedisTemplate.delete("seckill:order:" + v.getId());
        }
        return Result.ok("已重置 " + list.size() + " 张秒杀券库存");
    }
}
