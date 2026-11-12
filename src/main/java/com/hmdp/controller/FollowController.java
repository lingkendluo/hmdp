package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.entity.Follow;
import com.hmdp.service.FollowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /** 关注 / 取关 */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId,
                         @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    /** 是否已关注 */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /** 共同关注 */
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id) {
        return followService.followCommons(id);
    }

    /** 我关注的人 */
    @GetMapping("/follows/{userId}")
    public Result queryFollows(@PathVariable Long userId) {
        return followService.queryFollows(userId);
    }

    /** 我的粉丝 */
    @GetMapping("/fans/{userId}")
    public Result queryFans(@PathVariable Long userId) {
        return followService.queryFans(userId);
    }

    /** 关注数与粉丝数 */
    @GetMapping("/counts/{userId}")
    public Result counts(@PathVariable Long userId) {
        long follows = followService.query().eq("user_id", userId).count();
        long fans = followService.query().eq("follow_user_id", userId).count();
        java.util.Map<String, Object> data = new java.util.HashMap<>(4);
        data.put("follows", follows);
        data.put("fans", fans);
        return Result.ok(data);
    }
}
