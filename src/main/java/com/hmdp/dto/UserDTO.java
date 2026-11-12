package com.hmdp.dto;

import lombok.Data;

/**
 * 登录后缓存在 Redis 中的用户信息
 */
@Data
public class UserDTO {

    private Long id;

    private String nickName;

    private String icon;
}
