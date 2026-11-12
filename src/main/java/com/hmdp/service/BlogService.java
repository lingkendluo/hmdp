package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Blog;

public interface BlogService extends IService<Blog> {

    /** 发布探店笔记，并推送到粉丝的 Feed 流 */
    Result saveBlog(Blog blog);

    /** 热门笔记 */
    Result queryHotBlog(Integer current);

    /** 我的笔记 */
    Result queryMyBlog(Integer current);

    /** 店铺下的笔记 */
    Result queryBlogOfShop(Long shopId, Integer current);

    /** 用户的笔记 */
    Result queryBlogOfUser(Long userId, Integer current);

    /** 笔记详情 */
    Result queryBlogById(Long id);

    /** 点赞 / 取消点赞 */
    Result likeBlog(Long id);

    /** 点赞排行榜（前 5 名） */
    Result queryBlogLikes(Long id);

    /** 关注流滚动分页 */
    Result queryBlogOfFollow(Long maxTime, Integer offset);
}
