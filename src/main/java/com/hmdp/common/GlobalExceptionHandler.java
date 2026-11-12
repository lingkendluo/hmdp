package com.hmdp.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("系统异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "服务器开小差了，请稍后再试";
        }
        return Result.fail(msg);
    }
}
