package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.service.VoucherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /** 店铺可用券 */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }

    /** 全部秒杀券 */
    @GetMapping("/seckill/list")
    public Result querySeckillList() {
        return voucherService.querySeckillList();
    }

    /** 券详情 */
    @GetMapping("/{id}")
    public Result queryVoucherById(@PathVariable("id") Long id) {
        return voucherService.queryVoucherById(id);
    }

    /** 新增秒杀券 */
    @PostMapping("/seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        return voucherService.addSeckillVoucher(voucher);
    }

    /** 重置秒杀库存（演示用） */
    @PostMapping("/reset-stock")
    public Result resetStock() {
        return voucherService.resetStock();
    }
}
