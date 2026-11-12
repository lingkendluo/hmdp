package com.hmdp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单落库（独立事务），与秒杀主流程解耦。
 * 单独抽出来是为了让 @Transactional 通过 Spring 代理生效。
 */
@Slf4j
@Service
public class VoucherOrderTxService {

    private final VoucherOrderMapper voucherOrderMapper;
    private final SeckillVoucherMapper seckillVoucherMapper;

    public VoucherOrderTxService(VoucherOrderMapper voucherOrderMapper, SeckillVoucherMapper seckillVoucherMapper) {
        this.voucherOrderMapper = voucherOrderMapper;
        this.seckillVoucherMapper = seckillVoucherMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(VoucherOrder order) {
        // 1. 数据库层兜底：一人一单
        Long count = voucherOrderMapper.selectCount(Wrappers.<VoucherOrder>lambdaQuery()
                .eq(VoucherOrder::getUserId, order.getUserId())
                .eq(VoucherOrder::getVoucherId, order.getVoucherId()));
        if (count != null && count > 0) {
            throw new IllegalStateException("重复下单");
        }

        // 2. 扣减库存（带 stock > 0 条件，防止超卖）
        int updated = seckillVoucherMapper.update(null, Wrappers.<SeckillVoucher>lambdaUpdate()
                .setSql("stock = stock - 1")
                .eq(SeckillVoucher::getVoucherId, order.getVoucherId())
                .gt(SeckillVoucher::getStock, 0));
        if (updated == 0) {
            throw new IllegalStateException("库存不足");
        }

        // 3. 保存订单
        order.setStatus(1);
        order.setPayType(1);
        voucherOrderMapper.insert(order);
    }
}
