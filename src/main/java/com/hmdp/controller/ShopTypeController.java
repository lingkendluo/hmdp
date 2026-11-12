package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.service.ShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    private final ShopTypeService shopTypeService;

    public ShopTypeController(ShopTypeService shopTypeService) {
        this.shopTypeService = shopTypeService;
    }

    @GetMapping("/list")
    public Result queryTypeList() {
        return shopTypeService.queryList();
    }
}
