package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.mapper.TopicMapper;
import com.example.thesisconnectback.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课题服务实现类
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Autowired
    private TopicMapper topicMapper;

    @Override
    public List<Topic> findByTeacherId(Long teacherId) {
        return topicMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Topic> findByMajor(String major) {
        return topicMapper.findByMajor(major);
    }

    @Override
    public List<Topic> findByDifficulty(String difficulty) {
        return topicMapper.findByDifficulty(difficulty);
    }

    @Override
    public List<Topic> findByStatus(String status) {
        return topicMapper.findByStatus(status);
    }

    @Override
    public List<Topic> searchByKeyword(String keyword) {
        return topicMapper.searchByKeyword(keyword);
    }

    @Override
    public void incrementViewCount(Long topicId) {
        topicMapper.incrementViewCount(topicId);
    }

    @Override
    public void updateSelectedCount(Long topicId, Integer selectedCount) {
        topicMapper.updateSelectedCount(topicId, selectedCount);
    }

    @Override
    public boolean publishTopic(Topic topic) {
        topic.setStatus("active");
        topic.setSelectedCount(0);
        topic.setViewCount(0);
        topic.setRating(0.0);
        topic.setCreateTime(LocalDateTime.now());
        topic.setUpdateTime(LocalDateTime.now());
        return save(topic);
    }

    @Override
    public boolean updateTopic(Topic topic) {
        topic.setUpdateTime(LocalDateTime.now());
        return updateById(topic);
    }

    @Override
    public boolean deleteTopic(Long topicId) {
        return removeById(topicId);
    }

    @Override
    public Map<String, Object> getTopicStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总课题数
        long totalTopics = count();
        stats.put("totalTopics", totalTopics);
        
        // 各状态课题数
        List<Topic> activeTopics = findByStatus("active");
        List<Topic> completedTopics = findByStatus("completed");
        List<Topic> pausedTopics = findByStatus("paused");
        
        stats.put("activeTopics", activeTopics.size());
        stats.put("completedTopics", completedTopics.size());
        stats.put("pausedTopics", pausedTopics.size());
        
        // 各难度课题数
        List<Topic> easyTopics = findByDifficulty("easy");
        List<Topic> mediumTopics = findByDifficulty("medium");
        List<Topic> hardTopics = findByDifficulty("hard");
        
        stats.put("easyTopics", easyTopics.size());
        stats.put("mediumTopics", mediumTopics.size());
        stats.put("hardTopics", hardTopics.size());
        
        return stats;
    }

    @Override
    public List<Topic> getPopularTopics(Integer limit) {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("view_count");
        queryWrapper.last("LIMIT " + limit);
        return list(queryWrapper);
    }
}
