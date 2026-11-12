package com.hmdp.service;

import com.hmdp.common.Result;

public interface SignService {

    /** 今日签到（BitMap） */
    Result sign();

    /** 连续签到天数 */
    Result signCount();

    /** 本月签到日历 */
    Result signStatus();
}
