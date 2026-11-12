package com.hmdp.dto;

import lombok.Data;

import java.util.List;

/**
 * 滚动分页结果（用于关注流 Feed）
 */
@Data
public class ScrollResult {

    private List<?> list;

    /** 本次结果中的最小时间戳，下次请求作为 maxTime 传入 */
    private Long minTime;

    /** 偏移量：与 minTime 相同的记录有几条 */
    private Integer offset;
}
