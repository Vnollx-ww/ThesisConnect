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
    
    /**
     * 根据教师获取学生列表（包含选题信息）
     */
    @Select("SELECT DISTINCT u.*, s.topic_id, s.topic_title, s.selection_time, s.status as selection_status, " +
            "s.progress, s.progress_description, s.problems, s.final_grade, " +
            "t.description as topic_description, t.major, t.difficulty, t.max_students, " +
            "t.deadline, t.requirements, t.expected_outcome, t.view_count, t.rating, " +
            "t.status as topic_status, t.selected_count " +
            "FROM sys_user u " +
            "INNER JOIN sys_selection s ON u.id = s.student_id " +
            "INNER JOIN sys_topic t ON s.topic_id = t.id " +
            "WHERE t.teacher_id = #{teacherId} AND u.role = 'student' AND u.deleted = 0 AND s.deleted = 0")
    List<Map<String, Object>> getStudentsByTeacherWithSelection(@Param("teacherId") Long teacherId);
    
    /**
     * 获取活跃学生（有选题的学生）
     */
    @Select("SELECT DISTINCT u.* FROM sys_user u " +
            "INNER JOIN sys_selection s ON u.id = s.student_id " +
            "WHERE u.role = 'student' AND u.deleted = 0 AND s.status = 'active'")
    List<User> getActiveStudents();
    
    /**
     * 获取已完成学生
     */
    @Select("SELECT DISTINCT u.* FROM sys_user u " +
            "INNER JOIN sys_selection s ON u.id = s.student_id " +
            "WHERE u.role = 'student' AND u.deleted = 0 AND s.status = 'completed'")
    List<User> getCompletedStudents();
    
    /**
     * 获取平均进度
     */
    @Select("SELECT AVG(s.progress) FROM sys_selection s " +
            "INNER JOIN sys_user u ON s.student_id = u.id " +
            "WHERE u.role = 'student' AND u.deleted = 0 AND s.status = 'active'")
    Double getAverageProgress();
}
