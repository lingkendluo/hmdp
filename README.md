# 黑马点评 hmdp

黑马点评（Redis 实战项目）的完整复刻：**Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis 5**，
后端 10 个核心模块全部实现，前端是一套重新设计的移动端界面（原生 HTML/CSS/JS，无构建、无 CDN 依赖）。

打开 `http://localhost:8081` 即可使用，不需要单独部署前端。

---

## 一、快速开始

### 1. 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | 已在用 |
| Maven | 3.9+ | 已在用 |
| MySQL | 8.0 | `root / 123456`，端口 3306 |
| Redis | 5.0+ | 已解压在 `D:\DevTools\redis`，端口 6379 |

### 2. 三步启动

```text
① 启动 Redis
   D:\DevTools\redis\redis-server.exe D:\DevTools\redis\redis.windows.conf

② 初始化数据库（只需一次，会重建 hmdp 库）
   双击 init-db.bat

③ 启动应用
   双击 start.bat  →  自动打开 http://localhost:8081
```

也可以手动执行：

```bash
mvn -DskipTests package
java -jar target/hmdp-1.0.0.jar --server.port=8081
```

> 注意：如果所在环境设置了 `SERVER_PORT` 之类的环境变量，Spring Boot 会用它覆盖配置文件里的端口，
> 此时务必用 `--server.port=8081` 显式指定。

### 3. 演示账号

| 项目 | 值 |
|---|---|
| 手机号 | `13800000001`（罗耀）～ `13800000020` |
| 验证码 | 点击「获取验证码」后**自动填入**并提示（演示环境不发短信） |

---

## 二、功能清单

| 模块 | 已实现功能 | 对应 Redis / 技术点 |
|---|---|---|
| 短信登录 | 验证码发送与校验、登录注册一体、token 续期、退出登录 | Redis Hash 存 token（30 分钟滑动过期）、双拦截器、ThreadLocal |
| 商户缓存 | 分类缓存、店铺详情缓存、缓存预热 | 旁路缓存、**空值缓存**防穿透、**互斥锁**防击穿、**TTL 随机**防雪崩 |
| 逻辑过期 | `/shop/{id}/logical` 演示另一种击穿方案 | 逻辑过期 + 异步重建线程池 |
| 附近商户 | 按类型 + 坐标检索，距离由近到远 | **Redis GEO**（GEORADIUS） |
| 优惠券秒杀 | 秒杀列表、券详情、抢购下单、我的订单、模拟支付 | **全局唯一 ID**（时间戳+序列号）、**Lua 原子扣减**、一人一单、阻塞队列异步下单 |
| 探店笔记 | 发布、详情、按店铺/用户分页、热门榜 | 分页查询、Feed 推送 |
| 点赞 | 点赞/取消、点赞排行榜 | **Redis SortedSet**（按时间排序） |
| 好友关注 | 关注/取关、是否关注、共同关注、关注/粉丝列表 | **Redis Set 交集**（SINTER） |
| 关注流 | 滚动分页加载关注的人的新笔记 | **SortedSet + ZREVRANGEBYSCORE**，推模式 |
| 用户签到 | 每日签到、连续天数、本月签到日历 | **Redis BitMap**（SETBIT / GETBIT） |
| UV 统计 | 店铺浏览量 | **HyperLogLog**（PFADD / PFCOUNT） |
| 图片上传 | 笔记配图上传 | 本地文件存储 + 静态资源映射 |

---

## 三、Redis Key 设计

| Key | 类型 | 说明 |
|---|---|---|
| `login:code:{phone}` | String | 登录验证码，TTL 2 分钟 |
| `login:token:{token}` | Hash | 登录用户（id / nickName / icon），TTL 30 分钟，每次请求续期 |
| `cache:shopType:list` | String(JSON) | 店铺分类，TTL 60 分钟 |
| `cache:shop:{id}` | String(JSON) | 店铺缓存，TTL 30 分钟 + 随机 0~300s |
| `lock:shop:{id}` | String | 缓存重建互斥锁，10 秒 |
| `seckill:stock:{voucherId}` | String | 秒杀库存，启动预热 |
| `seckill:order:{voucherId}` | Set | 已下单用户集合，实现一人一单 |
| `lock:order:{userId}` | String | 下单兜底分布式锁 |
| `blog:liked:{blogId}` | SortedSet | 点赞用户，score 为点赞时间戳 |
| `feed:{userId}` | SortedSet | 关注流收件箱，score 为笔记发布时间戳 |
| `follow:{userId}` | Set | 我关注的人，用于共同关注求交集 |
| `sign:{userId}:{yyyyMM}` | BitMap | 签到，bit 下标 = 日期 - 1 |
| `uv:shop:{id}` | HyperLogLog | 店铺 UV |
| `shop:geo:{typeId}` | GEO | 店铺坐标，按类型分组 |
| `icr:order:{yyyy:MM:dd}` | String | 全局 ID 的当日序列号 |

秒杀 Lua 脚本在 `src/main/resources/lua/seckill.lua`，一次原子操作完成「库存校验 + 一人一单 + 扣减」。

---

## 四、接口一览

### 用户
```
POST   /user/code?phone=          发送验证码
POST   /user/login                登录 {phone, code}
POST   /user/logout               退出
GET    /user/me                   当前用户
GET    /user/{id}                 用户主页
PUT    /user/info                 修改资料
```

### 商户
```
GET    /shop-type/list                         分类列表
GET    /shop/{id}                              店铺详情（记录 UV）
GET    /shop/{id}/logical                      逻辑过期版本
GET    /shop/{id}/uv                           店铺 UV
GET    /shop/of/type?typeId=&current=&x=&y=    按类型分页（带坐标则按距离）
GET    /shop/of/name?name=                     搜索
GET    /shop/hot                               热门
POST   /shop                                   新增
PUT    /shop                                   修改
POST   /shop/preheat                           重新预热 GEO
```

### 优惠券 / 订单
```
GET    /voucher/list/{shopId}      店铺可用券
GET    /voucher/seckill/list       全部秒杀券
GET    /voucher/{id}               券详情
POST   /voucher/seckill            新增秒杀券
POST   /voucher/reset-stock        重置库存（演示用）
POST   /voucher-order/seckill/{voucherId}   秒杀下单
GET    /voucher-order/my           我的订单
GET    /voucher-order/{orderId}    订单详情
PUT    /voucher-order/pay/{orderId} 模拟支付
```

### 笔记 / 关注 / 签到
```
POST   /blog                        发布笔记
GET    /blog/hot                    热门
GET    /blog/{id}                   详情
GET    /blog/of/shop?shopId=        店铺笔记
GET    /blog/of/user?userId=        用户笔记
GET    /blog/of/me                  我的笔记
GET    /blog/of/follow              关注流（滚动分页）
PUT    /blog/like/{id}              点赞
GET    /blog/likes/{id}             点赞榜
PUT    /follow/{id}/{isFollow}      关注/取关
GET    /follow/or/not/{id}          是否关注
GET    /follow/common/{id}          共同关注
GET    /follow/follows/{userId}     关注列表
GET    /follow/fans/{userId}        粉丝列表
GET    /follow/counts/{userId}      关注数/粉丝数
POST   /sign                        签到
POST   /sign/count                  连续天数
GET    /sign/status                 本月日历
POST   /upload/blog                 上传图片
```

---

## 五、项目结构

```
hmdp
├── sql/
│   ├── schema.sql            建库建表（11 张表）
│   └── data.sql              演示数据
├── src/main/java/com/hmdp/
│   ├── common/               Result / 常量 / 全局异常
│   ├── config/               MyBatis-Plus / MVC / 启动预热
│   ├── controller/           9 个 Controller
│   ├── dto/                  LoginFormDTO / UserDTO / BlogVO / ScrollResult ...
│   ├── entity/               11 个实体
│   ├── mapper/               MyBatis-Plus Mapper
│   ├── service/              业务层（接口 + impl）
│   └── utils/                CacheClient / RedisIdWorker / SimpleRedisLock / 拦截器
├── src/main/resources/
│   ├── application.yaml
│   ├── lua/seckill.lua
│   └── static/               前端（index / shop / voucher / me / feed / orders ...）
├── tools/gen_assets.py       生成店铺封面图 + data.sql
├── start.bat                 一键启动
└── init-db.bat               初始化数据库
```

---

## 六、几个实现细节

**缓存三件套**

- 穿透：查不到时写入空字符串 `""`，TTL 2 分钟，避免每次都打数据库
- 击穿：互斥锁（`SETNX` + 双重检查）或逻辑过期（异步重建，先返回旧值）
- 雪崩：TTL 加 0~300 秒随机值

**秒杀下单流程**

```
请求 → Lua 脚本（库存>0? 没买过? → 扣库存 + 记录用户）
     → 通过则生成全局唯一 ID，订单入阻塞队列，立即返回订单号
     → 后台线程消费队列，事务落库（扣库存 + 插订单，DB 兜底防超卖）
     → 失败则回滚 Redis 的库存与用户标记
```

**全局唯一 ID**

`31 位时间戳（秒） + 32 位当日序列号`，序列号用 Redis `INCR` 按天生成，保证趋势递增、不重复。

---

## 七、常见问题

**启动报 `Port xxxx was already in use`**
环境里有 `SERVER_PORT` / `SERVER__PORT` 变量时会覆盖配置，用 `--server.port=8081` 显式指定。

**页面能打开但数据为空**
检查 Redis 是否启动（`redis-cli ping` 返回 PONG）、MySQL 是否导入数据。

**抢券提示「每人限购一张」**
这是设计如此。想重复演示，执行 `POST /voucher/reset-stock` 会重置 Redis 库存与购买标记，
或换一个手机号登录。

**店铺图片是纯色块**
`imgs/shop/*.svg` 是脚本生成的抽象封面，不是真实照片。替换同名文件即可换成自己的图。
