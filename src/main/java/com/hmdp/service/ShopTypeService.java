package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.ShopType;

public interface ShopTypeService extends IService<ShopType> {

    /** 查询商户分类列表（走 Redis 缓存） */
    Result queryList();
}
