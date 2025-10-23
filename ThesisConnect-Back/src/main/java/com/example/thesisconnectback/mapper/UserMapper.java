package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 根据角色查询用户列表
     */
    @Select("SELECT * FROM sys_user WHERE role = #{role} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByRole(@Param("role") String role);

    /**
     * 根据状态查询用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByStatus(@Param("status") Integer status);

    /**
     * 统计各角色用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM sys_user WHERE deleted = 0 GROUP BY role")
    List<Object> countByRole();

    /**
     * 更新用户最后登录时间
     */
    @Select("UPDATE sys_user SET last_login_time = NOW(), login_count = login_count + 1 WHERE id = #{userId}")
    void updateLastLoginTime(@Param("userId") Long userId);

    /**
     * 获取用户增长趋势数据
     */
    @Select({
        "<script>",
        "SELECT ",
        "<choose>",
        "<when test='period == \"week\"'>",
        "CONCAT(DATE_FORMAT(create_time, '%Y年'), '第', DATE_FORMAT(create_time, '%u'), '周') as timeLabel, COUNT(*) as userCount",
        "</when>",
        "<when test='period == \"month\"'>",
        "CONCAT(DATE_FORMAT(create_time, '%Y年'), DATE_FORMAT(create_time, '%m'), '月') as timeLabel, COUNT(*) as userCount",
        "</when>",
        "<when test='period == \"year\"'>",
        "CONCAT(DATE_FORMAT(create_time, '%Y'), '年') as timeLabel, COUNT(*) as userCount",
        "</when>",
        "<otherwise>",
        "DATE_FORMAT(create_time, '%Y-%m') as timeLabel, COUNT(*) as userCount",
        "</otherwise>",
        "</choose>",
        "FROM sys_user WHERE deleted = 0 AND create_time >= DATE_SUB(NOW(), INTERVAL ",
        "<choose>",
        "<when test='period == \"week\"'>12 WEEK</when>",
        "<when test='period == \"month\"'>12 MONTH</when>",
        "<when test='period == \"year\"'>5 YEAR</when>",
        "<otherwise>12 MONTH</otherwise>",
        "</choose>",
        ") GROUP BY timeLabel ORDER BY timeLabel",
        "</script>"
    })
    List<Map<String, Object>> getUserGrowthTrend(@Param("period") String period);
}
