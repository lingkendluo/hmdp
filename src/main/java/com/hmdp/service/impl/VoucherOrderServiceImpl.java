package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.RedisConstants;
import com.hmdp.common.Result;
import com.hmdp.dto.VoucherOrderVO;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.VoucherOrderService;
import com.hmdp.service.VoucherOrderTxService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 秒杀下单：
 * 1. Lua 脚本原子校验「库存 + 一人一单」
 * 2. 校验通过后把订单丢进阻塞队列，立即返回订单号（异步下单，削峰）
 * 3. 后台线程消费队列，真正写数据库
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements VoucherOrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /** 阻塞队列，容量 10 万 */
    private static final BlockingQueue<VoucherOrder> ORDER_QUEUE = new ArrayBlockingQueue<>(1024 * 100);

    private static final ExecutorService SECKILL_ORDER_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "seckill-order-handler");
                t.setDaemon(true);
                return t;
            });

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisIdWorker redisIdWorker;
    private final VoucherOrderTxService txService;

    public VoucherOrderServiceImpl(StringRedisTemplate stringRedisTemplate,
                                   RedisIdWorker redisIdWorker,
                                   VoucherOrderTxService txService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisIdWorker = redisIdWorker;
        this.txService = txService;
    }

    @PostConstruct
    public void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
        log.info("秒杀订单处理线程已启动");
    }

    // ------------------------------------------------------- 队列消费

    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    VoucherOrder order = ORDER_QUEUE.take();
                    handleOrder(order);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("秒杀订单处理异常", e);
                }
            }
        }
    }

    private void handleOrder(VoucherOrder order) {
        // 集群环境下同一用户可能被路由到不同节点，这里再加一层分布式锁兜底
        SimpleRedisLock lock = new SimpleRedisLock(RedisConstants.LOCK_ORDER_KEY + order.getUserId(), stringRedisTemplate);
        if (!lock.tryLock(5)) {
            log.warn("获取订单锁失败，回滚 Redis 标记，orderId={}", order.getId());
            rollbackRedis(order);
            return;
        }
        try {
            txService.createOrder(order);
            log.info("订单创建成功 orderId={}, userId={}, voucherId={}",
                    order.getId(), order.getUserId(), order.getVoucherId());
        } catch (Exception e) {
            log.error("订单落库失败，回滚 Redis 标记 orderId={}", order.getId(), e);
            rollbackRedis(order);
        } finally {
            lock.unlock();
        }
    }

    private void rollbackRedis(VoucherOrder order) {
        stringRedisTemplate.opsForValue().increment(RedisConstants.SECKILL_STOCK_KEY + order.getVoucherId());
        stringRedisTemplate.opsForSet().remove("seckill:order:" + order.getVoucherId(),
                order.getUserId().toString());
    }

    // ------------------------------------------------------- 秒杀入口

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }

        long orderId = redisIdWorker.nextId("order");

        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        String orderKey = "seckill:order:" + voucherId;

        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Arrays.asList(stockKey, orderKey), userId.toString());

        int code = result == null ? -1 : result.intValue();
        switch (code) {
            case 0 -> {
                VoucherOrder order = new VoucherOrder();
                order.setId(orderId);
                order.setUserId(userId);
                order.setVoucherId(voucherId);
                order.setPayType(1);
                order.setStatus(1);
                order.setCreateTime(LocalDateTime.now());
                ORDER_QUEUE.add(order);
                return Result.ok(orderId);
            }
            case 1 -> {
                return Result.fail("库存不足");
            }
            case 2 -> {
                return Result.fail("每人限购一张，你已经抢过了");
            }
            case 3 -> {
                return Result.fail("秒杀尚未开始或已结束");
            }
            default -> {
                return Result.fail("秒杀失败，请稍后重试");
            }
        }
    }

    @Override
    public Result queryMyOrders() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        List<VoucherOrderVO> list = baseMapper.listVOByUserId(userId);
        return Result.ok(list);
    }

    @Override
    public Result queryOrderDetail(Long orderId) {
        VoucherOrderVO vo = baseMapper.getVOById(orderId);
        if (vo == null) {
            return Result.fail("订单不存在");
        }
        if (!vo.getUserId().equals(UserHolder.getUserId())) {
            return Result.fail("无权查看该订单");
        }
        return Result.ok(vo);
    }

    @Override
    public Result payOrder(Long orderId) {
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(UserHolder.getUserId())) {
            return Result.fail("无权操作该订单");
        }
        if (order.getStatus() != 1) {
            return Result.fail("订单状态不支持支付");
        }
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        return Result.ok();
    }
}
