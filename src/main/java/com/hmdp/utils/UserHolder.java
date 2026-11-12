package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

/**
 * 基于 ThreadLocal 保存当前登录用户
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> TL = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        TL.set(user);
    }

    public static UserDTO getUser() {
        return TL.get();
    }

    public static Long getUserId() {
        UserDTO user = TL.get();
        return user == null ? null : user.getId();
    }

    public static void removeUser() {
        TL.remove();
    }
}
