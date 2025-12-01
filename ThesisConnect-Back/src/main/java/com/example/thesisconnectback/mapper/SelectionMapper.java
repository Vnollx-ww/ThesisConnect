package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.Selection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选题记录Mapper接口
 */
@Mapper
public interface SelectionMapper extends BaseMapper<Selection> {

    /**
     * 根据学生ID查询选题记录
     */
    @Select("SELECT * FROM sys_selection WHERE student_id = #{studentId} AND deleted = 0 ORDER BY create_time DESC")
    List<Selection> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据课题ID查询选题记录
     */
    @Select("SELECT * FROM sys_selection WHERE topic_id = #{topicId} AND deleted = 0 ORDER BY create_time DESC")
    List<Selection> findByTopicId(@Param("topicId") Long topicId);

    /**
     * 根据教师ID查询选题记录（排除已拒绝的）
     */
    @Select("SELECT * FROM sys_selection WHERE teacher_id = #{teacherId} AND deleted = 0 AND status != 'rejected' ORDER BY create_time DESC")
    List<Selection> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据状态查询选题记录
     */
    @Select("SELECT * FROM sys_selection WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Selection> findByStatus(@Param("status") String status);

    /**
     * 检查学生是否已选择课题（只统计已确认、进行中或已完成的）
     */
    @Select("SELECT COUNT(*) FROM sys_selection WHERE student_id = #{studentId} AND deleted = 0 AND status IN ('confirmed', 'active', 'completed')")
    int countByStudentId(@Param("studentId") Long studentId);

    /**
     * 检查课题是否已满员（只统计已确认、进行中或已完成的）
     */
    @Select("SELECT COUNT(*) FROM sys_selection WHERE topic_id = #{topicId} AND status IN ('confirmed', 'active', 'completed') AND deleted = 0")
    int countApprovedByTopicId(@Param("topicId") Long topicId);
    
    /**
     * 统计某个课题的已确认选题数量
     */
    @Select("SELECT COUNT(*) FROM sys_selection WHERE topic_id = #{topicId} AND status IN ('confirmed', 'active', 'completed') AND deleted = 0")
    int countConfirmedByTopicId(@Param("topicId") Long topicId);
    
    /**
     * 统计某个课题的所有选题数量（包括待审核和已审核的）
     */
    @Select("SELECT COUNT(*) FROM sys_selection WHERE topic_id = #{topicId} AND deleted = 0")
    int countByTopicId(@Param("topicId") Long topicId);

    /**
     * 统计各状态选题数量
     */
    @Select("SELECT status, COUNT(*) as count FROM sys_selection WHERE deleted = 0 GROUP BY status")
    List<Object> countByStatus();

    /**
     * 检查学生是否被该课题拒绝过
     */
    @Select("SELECT COUNT(*) FROM sys_selection WHERE student_id = #{studentId} AND topic_id = #{topicId} AND status = 'rejected' AND deleted = 0")
    int countRejectedByStudentAndTopic(@Param("studentId") Long studentId, @Param("topicId") Long topicId);
    
    /**
     * 统计教师指导的选题数量
     */
    @Select("SELECT teacher_id, teacher_name, COUNT(*) as count FROM sys_selection WHERE deleted = 0 GROUP BY teacher_id, teacher_name")
    List<Object> countByTeacher();
}
