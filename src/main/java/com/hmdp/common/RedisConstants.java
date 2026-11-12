package com.hmdp.common;

/**
 * Redis Key 常量（与黑马点评课程保持一致）
 */
public class RedisConstants {

    /** 登录验证码 */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    /** 登录用户 token */
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    /** 商户缓存 */
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L;

    /** 商户分类缓存 */
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shopType:";
    public static final Long CACHE_SHOP_TYPE_TTL = 60L;

    /** 缓存空值 TTL（解决缓存穿透） */
    public static final Long CACHE_NULL_TTL = 2L;

    /** 逻辑过期时间（演示缓存击穿方案） */
    public static final Long CACHE_SHOP_LOGICAL_TTL = 20L;

    /** 商户锁 */
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    /** 秒杀券库存 */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    /** 笔记点赞 */
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    /** 关注推送 Feed 流 */
    public static final String FEED_KEY = "feed:";

    /** 店铺 GEO */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /** 用户签到 */
    public static final String USER_SIGN_KEY = "sign:";

    /** 订单锁 */
    public static final String LOCK_ORDER_KEY = "lock:order:";

    /** 店铺 UV */
    public static final String SHOP_UV_KEY = "uv:shop:";
}
