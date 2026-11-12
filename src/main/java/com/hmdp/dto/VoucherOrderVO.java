package com.hmdp.dto;

import com.hmdp.entity.VoucherOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单视图对象：订单 + 券信息 + 店铺信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VoucherOrderVO extends VoucherOrder {

    private String voucherTitle;

    private String subTitle;

    private Long payValue;

    private Long actualValue;

    private Long shopId;

    private String shopName;

    private String shopImages;
}
