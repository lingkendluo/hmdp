package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.dto.VoucherOrderVO;
import com.hmdp.entity.VoucherOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    /** 我的订单（带券与店铺信息） */
    @Select("SELECT o.*, v.title AS voucher_title, v.sub_title, v.pay_value, v.actual_value, v.shop_id, " +
            "s.name AS shop_name, s.images AS shop_images " +
            "FROM tb_voucher_order o " +
            "LEFT JOIN tb_voucher v ON o.voucher_id = v.id " +
            "LEFT JOIN tb_shop s ON v.shop_id = s.id " +
            "WHERE o.user_id = #{userId} " +
            "ORDER BY o.create_time DESC")
    List<VoucherOrderVO> listVOByUserId(@Param("userId") Long userId);

    /** 订单详情 */
    @Select("SELECT o.*, v.title AS voucher_title, v.sub_title, v.pay_value, v.actual_value, v.shop_id, " +
            "s.name AS shop_name, s.images AS shop_images " +
            "FROM tb_voucher_order o " +
            "LEFT JOIN tb_voucher v ON o.voucher_id = v.id " +
            "LEFT JOIN tb_shop s ON v.shop_id = s.id " +
            "WHERE o.id = #{id}")
    VoucherOrderVO getVOById(@Param("id") Long id);
}
