package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 发送手机验证码 */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    /** 验证码登录 */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "authorization", required = false) String token) {
        return userService.logout(token);
    }

    /** 当前登录用户 */
    @GetMapping("/me")
    public Result me() {
        return userService.me();
    }

    /** 用户主页 */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long id) {
        return userService.queryUserById(id);
    }

    /** 修改个人资料 */
    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody UserInfo userInfo) {
        return userService.updateUserInfo(userInfo);
    }
}
