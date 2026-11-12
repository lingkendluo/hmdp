package com.hmdp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 逻辑过期包装：value 为真实数据，expireTime 为逻辑过期时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisData {

    private LocalDateTime expireTime;

    private Object data;
}
