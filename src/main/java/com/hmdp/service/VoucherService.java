package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Voucher;

public interface VoucherService extends IService<Voucher> {

    /** 店铺可用券列表 */
    Result queryVoucherOfShop(Long shopId);

    /** 全部秒杀券 */
    Result querySeckillList();

    /** 券详情 */
    Result queryVoucherById(Long id);

    /** 新增秒杀券（券 + 库存） */
    Result addSeckillVoucher(Voucher voucher);

    /** 秒杀库存预热到 Redis */
    void preheatSeckillStock();

    /** 重置秒杀库存（演示用） */
    Result resetStock();
}
