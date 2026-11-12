package com.hmdp.controller;

import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.ShopService;
import com.hmdp.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;

    public ShopController(ShopService shopService, StringRedisTemplate stringRedisTemplate) {
        this.shopService = shopService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 店铺详情：同时记录 UV（HyperLogLog） */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id, HttpServletRequest request) {
        String uvKey = RedisConstants.SHOP_UV_KEY + id;
        Long userId = UserHolder.getUserId();
        String visitor = userId != null ? "u" + userId : "ip" + clientIp(request);
        stringRedisTemplate.opsForHyperLogLog().add(uvKey, visitor);
        return shopService.queryById(id);
    }

    /** 逻辑过期方案演示 */
    @GetMapping("/{id}/logical")
    public Result queryShopWithLogicalExpire(@PathVariable("id") Long id) {
        return shopService.queryByIdWithLogicalExpire(id);
    }

    /** 店铺 UV */
    @GetMapping("/{id}/uv")
    public Result queryUv(@PathVariable("id") Long id) {
        Long size = stringRedisTemplate.opsForHyperLogLog().size(RedisConstants.SHOP_UV_KEY + id);
        return Result.ok(size == null ? 0 : size);
    }

    /** 按类型分页，带坐标时按距离排序 */
    @GetMapping("/of/type")
    public Result queryShopByType(@RequestParam(value = "typeId", required = false) Integer typeId,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current,
                                  @RequestParam(value = "x", required = false) Double x,
                                  @RequestParam(value = "y", required = false) Double y) {
        return shopService.queryShopByType(typeId, current, x, y);
    }

    /** 按名称搜索 */
    @GetMapping("/of/name")
    public Result queryShopByName(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return shopService.queryShopByName(name, current);
    }

    /** 首页热门 */
    @GetMapping("/hot")
    public Result queryHotShops(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return shopService.queryHotShops(current);
    }

    /** 新增店铺 */
    @PostMapping
    public Result addShop(@RequestBody Shop shop) {
        return shopService.addShop(shop);
    }

    /** 更新店铺 */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.updateShop(shop);
    }

    /** 手动预热 GEO */
    @PostMapping("/preheat")
    public Result preheat() {
        shopService.preheatGeo();
        return Result.ok("GEO 预热完成");
    }

    /** 逻辑过期数据预热 */
    @PostMapping("/preheat/{id}")
    public Result preheatOne(@PathVariable Long id,
                             @RequestParam(defaultValue = "20") Long seconds) {
        shopService.saveShop2Redis(id, seconds);
        return Result.ok();
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
