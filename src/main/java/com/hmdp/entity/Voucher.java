package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private String title;

    private String subTitle;

    private String rules;

    /** 支付金额，单位分 */
    private Long payValue;

    /** 抵扣金额，单位分 */
    private Long actualValue;

    /** 0 普通券，1 秒杀券 */
    private Integer type;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ---- 以下字段来自 tb_seckill_voucher，非当前表字段 ----

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer stock;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private LocalDateTime beginTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private LocalDateTime endTime;

    /** 所属店铺图片，仅用于展示 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String shopImages;

    /** 所属店铺名称，仅用于展示 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String shopName;
}
