package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Shop;

public interface ShopService extends IService<Shop> {

    /** 根据 id 查询店铺（互斥锁方案解决缓存击穿 + 空值解决穿透） */
    Result queryById(Long id);

    /** 根据 id 查询店铺（逻辑过期方案，需提前预热） */
    Result queryByIdWithLogicalExpire(Long id);

    /** 按类型分页查询店铺，传入坐标时按距离由近到远排序（Redis GEO） */
    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

    /** 按名称搜索店铺 */
    Result queryShopByName(String name, Integer current);

    /** 首页热门推荐 */
    Result queryHotShops(Integer current);

    /** 把店铺数据预热进 Redis（逻辑过期结构） */
    void saveShop2Redis(Long id, Long expireSeconds);

    /** 把全部店铺坐标写入 Redis GEO */
    void preheatGeo();

    /** 新增店铺 */
    Result addShop(Shop shop);

    /** 更新店铺 */
    Result updateShop(Shop shop);
}
