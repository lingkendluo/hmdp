package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;

public interface UserService extends IService<User> {

    /** 发送手机验证码 */
    Result sendCode(String phone);

    /** 验证码登录 */
    Result login(LoginFormDTO loginForm);

    /** 退出登录 */
    Result logout(String token);

    /** 当前登录用户信息 */
    Result me();

    /** 查询指定用户主页信息 */
    Result queryUserById(Long id);

    /** 修改个人资料 */
    Result updateUserInfo(UserInfo userInfo);
}
