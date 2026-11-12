package com.hmdp.dto;

import com.hmdp.entity.Blog;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 探店笔记视图对象：笔记 + 作者信息 + 店铺名 + 当前用户是否点赞
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BlogVO extends Blog {

    private String userName;

    private String userIcon;

    private String shopName;

    private Boolean isLiked;
}
