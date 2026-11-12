-- ============================================================
-- 黑马点评 hmdp 数据库结构（MySQL 8.0）
-- ============================================================
DROP DATABASE IF EXISTS hmdp;
CREATE DATABASE hmdp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hmdp;

-- 用户表
CREATE TABLE tb_user (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    phone       VARCHAR(11)     NOT NULL COMMENT '手机号码',
    password    VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '密码，加密存储',
    nick_name   VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '昵称，默认是用户id',
    icon        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '人物头像',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 用户详情表
CREATE TABLE tb_user_info (
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '主键，用户id',
    city        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '城市名称',
    introduce   VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '个人介绍',
    gender      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1男，2女，0未知',
    birthdays   DATE            NULL COMMENT '生日',
    credits     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '积分',
    level       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '等级',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户详情表';

-- 商户分类表
CREATE TABLE tb_shop_type (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '类型名称',
    icon        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '图标',
    sort        INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '顺序',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商户分类表';

-- 商户表
CREATE TABLE tb_shop (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(128)    NOT NULL COMMENT '商铺名称',
    type_id     BIGINT UNSIGNED NOT NULL COMMENT '商铺类型id',
    images      VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT '商铺图片，多张逗号分隔',
    area        VARCHAR(128)    NULL DEFAULT NULL COMMENT '商圈',
    address     VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '地址',
    x           DOUBLE          NOT NULL DEFAULT 0 COMMENT '经度',
    y           DOUBLE          NOT NULL DEFAULT 0 COMMENT '纬度',
    avg_price   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '均价',
    sold        INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '销量',
    comments    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '评论数量',
    score       INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '评分，1~5分，乘10保存',
    open_hours  VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '营业时间',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_type_id (type_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商户表';

-- 探店笔记表
CREATE TABLE tb_blog (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    shop_id     BIGINT UNSIGNED NOT NULL COMMENT '商户id',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    title       VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '标题',
    images      VARCHAR(2048)   NOT NULL DEFAULT '' COMMENT '图片，多张逗号分隔',
    content     VARCHAR(2048)   NOT NULL DEFAULT '' COMMENT '文字内容',
    liked       INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '点赞数量',
    comments    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '评论数量',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '探店笔记表';

-- 笔记评论表
CREATE TABLE tb_blog_comments (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '发布评论的用户id',
    blog_id     BIGINT UNSIGNED NOT NULL COMMENT '探店笔记id',
    parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联的1级评论id，若为0则代表是一级评论',
    answer_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复的评论id',
    content     VARCHAR(255)    NOT NULL COMMENT '评论内容',
    liked       INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '点赞数量',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态，0正常，1被举报，2逻辑删除',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_blog_id (blog_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '笔记评论表';

-- 关注表
CREATE TABLE tb_follow (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    follow_user_id BIGINT UNSIGNED NOT NULL COMMENT '关联的用户id',
    create_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '关注表';

-- 优惠券表
CREATE TABLE tb_voucher (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    shop_id      BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '商铺id',
    title        VARCHAR(255)    NOT NULL COMMENT '代金券标题',
    sub_title    VARCHAR(255)    NULL DEFAULT NULL COMMENT '副标题',
    rules        VARCHAR(1024)   NULL DEFAULT NULL COMMENT '使用规则',
    pay_value    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付金额，单位分',
    actual_value BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '抵扣金额，单位分',
    type         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '优惠券类型，0普通券，1秒杀券',
    status       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态，1上架，2下架，3过期',
    create_time  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '优惠券表';

-- 优惠券订单表
CREATE TABLE tb_voucher_order (
    id          BIGINT UNSIGNED NOT NULL COMMENT '主键，采用全局唯一ID生成',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '下单的用户id',
    voucher_id  BIGINT UNSIGNED NOT NULL COMMENT '购买的代金券id',
    pay_type    TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式，1余额，2支付宝，3微信',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1未支付，2已支付，3已核销，4已取消，5退款',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pay_time    TIMESTAMP       NULL DEFAULT NULL,
    use_time    TIMESTAMP       NULL DEFAULT NULL,
    refund_time TIMESTAMP       NULL DEFAULT NULL,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_voucher (user_id, voucher_id),
    KEY idx_user_id (user_id),
    KEY idx_voucher_id (voucher_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '优惠券订单表';

-- 秒杀优惠券表
CREATE TABLE tb_seckill_voucher (
    voucher_id  BIGINT UNSIGNED NOT NULL COMMENT '关联的优惠券id',
    stock       INT             NOT NULL DEFAULT 0 COMMENT '库存',
    begin_time  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    end_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (voucher_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀优惠券表';

-- 签到表
CREATE TABLE tb_sign (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    year        YEAR            NOT NULL COMMENT '签到的年',
    month       TINYINT         NOT NULL COMMENT '签到的月',
    `date`      DATE            NOT NULL COMMENT '签到的日期',
    is_backup   TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否补签，0否，1是',
    create_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_year_month (user_id, year, month)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '签到表';
