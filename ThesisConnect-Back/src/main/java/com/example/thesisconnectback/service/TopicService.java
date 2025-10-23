package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.Topic;

import java.util.List;
import java.util.Map;

/**
 * 课题服务接口
 */
public interface TopicService extends IService<Topic> {

    /**
     * 根据教师ID查询课题列表
     */
    List<Topic> findByTeacherId(Long teacherId);

    /**
     * 根据专业查询课题列表
     */
    List<Topic> findByMajor(String major);

    /**
     * 根据难度查询课题列表
     */
    List<Topic> findByDifficulty(String difficulty);

    /**
     * 根据状态查询课题列表
     */
    List<Topic> findByStatus(String status);

    /**
     * 搜索课题
     */
    List<Topic> searchByKeyword(String keyword);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long topicId);

    /**
     * 更新已选学生数
     */
    void updateSelectedCount(Long topicId, Integer selectedCount);

    /**
     * 发布课题
     */
    boolean publishTopic(Topic topic);

    /**
     * 更新课题
     */
    boolean updateTopic(Topic topic);

    /**
     * 删除课题
     */
    boolean deleteTopic(Long topicId);

    /**
     * 获取课题统计信息
     */
    Map<String, Object> getTopicStats();
    
    /**
     * 获取指定教师的课题统计信息
     */
    Map<String, Object> getTopicStatsByTeacher(Long teacherId);

    /**
     * 获取热门课题
     */
    List<Topic> getPopularTopics(Integer limit);

    /**
     * 获取课题难度分布
     */
    Map<String, Object> getTopicDifficultyDistribution();
}
