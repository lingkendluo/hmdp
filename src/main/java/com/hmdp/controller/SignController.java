package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.service.SignService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sign")
public class SignController {

    private final SignService signService;

    public SignController(SignService signService) {
        this.signService = signService;
    }

    /** 签到 */
    @PostMapping
    public Result sign() {
        return signService.sign();
    }

    /** 连续签到天数 */
    @PostMapping("/count")
    public Result signCount() {
        return signService.signCount();
    }

    /** 本月签到日历 */
    @GetMapping("/status")
    public Result signStatus() {
        return signService.signStatus();
    }
}
