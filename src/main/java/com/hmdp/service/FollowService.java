package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Follow;

public interface FollowService extends IService<Follow> {

    /** 关注 / 取关 */
    Result follow(Long followUserId, Boolean isFollow);

    /** 是否已关注 */
    Result isFollow(Long followUserId);

    /** 共同关注（Redis Set 交集） */
    Result followCommons(Long id);

    /** 我关注的人 */
    Result queryFollows(Long userId);

    /** 我的粉丝 */
    Result queryFans(Long userId);
}
