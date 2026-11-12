package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VoucherMapper extends BaseMapper<Voucher> {

    /** 查询店铺的全部可用券（普通券 + 秒杀券），秒杀券排在前面 */
    @Select("SELECT v.*, sv.stock, sv.begin_time, sv.end_time, s.images AS shop_images, s.name AS shop_name " +
            "FROM tb_voucher v " +
            "LEFT JOIN tb_seckill_voucher sv ON v.id = sv.voucher_id " +
            "LEFT JOIN tb_shop s ON v.shop_id = s.id " +
            "WHERE v.shop_id = #{shopId} AND v.status = 1 " +
            "ORDER BY v.type DESC, v.id")
    List<Voucher> listByShopId(@Param("shopId") Long shopId);

    /** 查询秒杀券详情（含库存与时间） */
    @Select("SELECT v.*, sv.stock, sv.begin_time, sv.end_time, s.images AS shop_images, s.name AS shop_name " +
            "FROM tb_voucher v " +
            "INNER JOIN tb_seckill_voucher sv ON v.id = sv.voucher_id " +
            "LEFT JOIN tb_shop s ON v.shop_id = s.id " +
            "WHERE v.id = #{id}")
    Voucher getSeckillById(@Param("id") Long id);

    /** 全部秒杀券 */
    @Select("SELECT v.*, sv.stock, sv.begin_time, sv.end_time, s.images AS shop_images, s.name AS shop_name " +
            "FROM tb_voucher v " +
            "INNER JOIN tb_seckill_voucher sv ON v.id = sv.voucher_id " +
            "LEFT JOIN tb_shop s ON v.shop_id = s.id " +
            "WHERE v.status = 1 AND sv.end_time > NOW() " +
            "ORDER BY sv.begin_time ASC")
    List<Voucher> listSeckill();
}
