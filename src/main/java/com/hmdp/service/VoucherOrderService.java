package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {

    /** 秒杀下单 */
    Result seckillVoucher(Long voucherId);

    /** 我的订单 */
    Result queryMyOrders();

    /** 订单详情 */
    Result queryOrderDetail(Long orderId);

    /** 支付（演示用，直接把状态改为已支付） */
    Result payOrder(Long orderId);
}
