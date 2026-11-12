package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hmdp.dto.BlogVO;
import com.hmdp.entity.Blog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlogMapper extends BaseMapper<Blog> {

    String BASE_SELECT = "SELECT b.*, u.nick_name AS user_name, u.icon AS user_icon, s.name AS shop_name " +
            "FROM tb_blog b " +
            "LEFT JOIN tb_user u ON b.user_id = u.id " +
            "LEFT JOIN tb_shop s ON b.shop_id = s.id ";

    /** 按店铺分页查询笔记 */
    @Select(BASE_SELECT + "WHERE b.shop_id = #{shopId} ORDER BY b.create_time DESC")
    IPage<BlogVO> pageByShopId(IPage<BlogVO> page, @Param("shopId") Long shopId);

    /** 按用户分页查询笔记 */
    @Select(BASE_SELECT + "WHERE b.user_id = #{userId} ORDER BY b.create_time DESC")
    IPage<BlogVO> pageByUserId(IPage<BlogVO> page, @Param("userId") Long userId);

    /** 查询单条笔记详情 */
    @Select(BASE_SELECT + "WHERE b.id = #{id}")
    BlogVO getVOById(@Param("id") Long id);

    /** 热门笔记 */
    @Select(BASE_SELECT + "ORDER BY b.liked DESC, b.create_time DESC LIMIT #{limit}")
    List<BlogVO> listHot(@Param("limit") int limit);

    /** 根据 id 集合查询笔记（Feed 流用），按传入时间顺序由业务层排序 */
    @Select("<script>" + BASE_SELECT +
            "WHERE b.id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<BlogVO> listVOByIds(@Param("ids") List<Long> ids);
}
