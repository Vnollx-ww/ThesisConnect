package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 课题Mapper接口
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    /**
     * 根据教师ID查询课题列表
     */
    @Select("SELECT * FROM sys_topic WHERE teacher_id = #{teacherId} AND deleted = 0 ORDER BY create_time DESC")
    List<Topic> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据专业查询课题列表
     */
    @Select("SELECT * FROM sys_topic WHERE major = #{major} AND deleted = 0 ORDER BY create_time DESC")
    List<Topic> findByMajor(@Param("major") String major);

    /**
     * 根据难度查询课题列表
     */
    @Select("SELECT * FROM sys_topic WHERE difficulty = #{difficulty} AND deleted = 0 ORDER BY create_time DESC")
    List<Topic> findByDifficulty(@Param("difficulty") String difficulty);

    /**
     * 根据状态查询课题列表
     */
    @Select("SELECT * FROM sys_topic WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Topic> findByStatus(@Param("status") String status);

    /**
     * 搜索课题（按标题和描述）
     */
    @Select("SELECT * FROM sys_topic WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND deleted = 0 ORDER BY create_time DESC")
    List<Topic> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 增加浏览量
     */
    @Update("UPDATE sys_topic SET view_count = view_count + 1 WHERE id = #{topicId}")
    void incrementViewCount(@Param("topicId") Long topicId);

    /**
     * 更新已选学生数
     */
    @Update("UPDATE sys_topic SET selected_count = #{selectedCount} WHERE id = #{topicId}")
    void updateSelectedCount(@Param("topicId") Long topicId, @Param("selectedCount") Integer selectedCount);

    /**
     * 统计各难度课题数量
     */
    @Select("SELECT difficulty, COUNT(*) as count FROM sys_topic WHERE deleted = 0 GROUP BY difficulty")
    List<Object> countByDifficulty();

    /**
     * 统计各专业课题数量
     */
    @Select("SELECT major, COUNT(*) as count FROM sys_topic WHERE deleted = 0 GROUP BY major")
    List<Object> countByMajor();
}
