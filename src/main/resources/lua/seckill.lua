-- 秒杀原子性校验脚本
-- KEYS[1] = 库存 key      seckill:stock:{voucherId}
-- KEYS[2] = 已下单用户集合 key  seckill:order:{voucherId}
-- ARGV[1] = 用户 id
-- 返回：0 成功  1 库存不足  2 重复下单  3 未预热

local stock = redis.call('get', KEYS[1])
if (not stock) then
    return 3
end
if (tonumber(stock) <= 0) then
    return 1
end
if (redis.call('sismember', KEYS[2], ARGV[1]) == 1) then
    return 2
end
redis.call('incrby', KEYS[1], -1)
redis.call('sadd', KEYS[2], ARGV[1])
return 0
