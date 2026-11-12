package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.service.VoucherOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    private final VoucherOrderService voucherOrderService;

    public VoucherOrderController(VoucherOrderService voucherOrderService) {
        this.voucherOrderService = voucherOrderService;
    }

    /** 秒杀下单 */
    @PostMapping("/seckill/{voucherId}")
    public Result seckillVoucher(@PathVariable("voucherId") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result queryMyOrders() {
        return voucherOrderService.queryMyOrders();
    }

    /** 订单详情 */
    @GetMapping("/{orderId}")
    public Result queryOrderDetail(@PathVariable("orderId") Long orderId) {
        return voucherOrderService.queryOrderDetail(orderId);
    }

    /** 模拟支付 */
    @PutMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable("orderId") Long orderId) {
        return voucherOrderService.payOrder(orderId);
    }
}
