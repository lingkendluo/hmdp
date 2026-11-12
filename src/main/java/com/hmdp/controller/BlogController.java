package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.entity.Blog;
import com.hmdp.service.BlogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /** 发布笔记 */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    /** 热门笔记 */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /** 我的笔记 */
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryMyBlog(current);
    }

    /** 店铺下的笔记 */
    @GetMapping("/of/shop")
    public Result queryBlogOfShop(@RequestParam("shopId") Long shopId,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryBlogOfShop(shopId, current);
    }

    /** 用户的笔记 */
    @GetMapping("/of/user")
    public Result queryBlogOfUser(@RequestParam("userId") Long userId,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryBlogOfUser(userId, current);
    }

    /** 关注流（滚动分页） */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(@RequestParam(value = "maxTime", required = false) Long maxTime,
                                    @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryBlogOfFollow(maxTime, offset);
    }

    /** 点赞 / 取消点赞 */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /** 点赞排行榜 */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    /** 笔记详情 */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }
}
