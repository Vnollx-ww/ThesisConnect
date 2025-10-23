package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.Progress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 进度记录Mapper接口
 */
@Mapper
public interface ProgressMapper extends BaseMapper<Progress> {

    /**
     * 根据选题ID获取进度记录
     */
    @Select("SELECT * FROM sys_progress WHERE selection_id = #{selectionId} AND deleted = 0 ORDER BY create_time DESC")
    List<Progress> findBySelectionId(@Param("selectionId") Long selectionId);

    /**
     * 根据学生ID获取进度记录
     */
    @Select("SELECT * FROM sys_progress WHERE student_id = #{studentId} AND deleted = 0 ORDER BY create_time DESC")
    List<Progress> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据课题ID获取进度记录
     */
    @Select("SELECT * FROM sys_progress WHERE topic_id = #{topicId} AND deleted = 0 ORDER BY create_time DESC")
    List<Progress> findByTopicId(@Param("topicId") Long topicId);

    /**
     * 获取最新进度
     */
    @Select("SELECT * FROM sys_progress WHERE selection_id = #{selectionId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    Progress getLatestProgress(@Param("selectionId") Long selectionId);

    /**
     * 获取进度统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalProgress, " +
            "AVG(percentage) as avgProgress, " +
            "COUNT(CASE WHEN percentage >= 80 THEN 1 END) as highProgressCount, " +
            "COUNT(CASE WHEN percentage < 30 THEN 1 END) as lowProgressCount " +
            "FROM sys_progress WHERE deleted = 0")
    Map<String, Object> getProgressStats();
}