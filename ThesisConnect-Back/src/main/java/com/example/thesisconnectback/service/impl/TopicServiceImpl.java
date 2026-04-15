package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.mapper.TopicMapper;
import com.example.thesisconnectback.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Topic service implementation class
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
        
        long totalTopics = count();
        stats.put("totalTopics", totalTopics);
        
        List<Topic> activeTopics = findByStatus("active");
        List<Topic> completedTopics = findByStatus("completed");
        List<Topic> pausedTopics = findByStatus("paused");
        
        stats.put("activeTopics", activeTopics.size());
        stats.put("completedTopics", completedTopics.size());
        stats.put("pausedTopics", pausedTopics.size());
        
        List<Topic> easyTopics = findByDifficulty("easy");
        List<Topic> mediumTopics = findByDifficulty("medium");
        List<Topic> hardTopics = findByDifficulty("hard");
        
        stats.put("easyTopics", easyTopics.size());
        stats.put("mediumTopics", mediumTopics.size());
        stats.put("hardTopics", hardTopics.size());
        
        long currentMonthTopics = getCurrentMonthTopicCount();
        long lastMonthTopics = getLastMonthTopicCount();
        double topicGrowth = lastMonthTopics > 0 ? ((double)(currentMonthTopics - lastMonthTopics) / lastMonthTopics) * 100 : 0;
        stats.put("topicGrowth", Math.round(topicGrowth * 100) / 100.0);
        
        return stats;
    }

    @Override
    public List<Topic> getPopularTopics(Integer limit) {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("view_count");
        queryWrapper.last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getTopicDifficultyDistribution() {
        Map<String, Object> distributionData = new HashMap<>();
        
        List<Topic> easyTopics = findByDifficulty("easy");
        List<Topic> mediumTopics = findByDifficulty("medium");
        List<Topic> hardTopics = findByDifficulty("hard");
        
        List<Map<String, Object>> pieData = new ArrayList<>();
        
        Map<String, Object> easyData = new HashMap<>();
        easyData.put("name", "Easy");
        easyData.put("value", easyTopics.size());
        pieData.add(easyData);
        
        Map<String, Object> mediumData = new HashMap<>();
        mediumData.put("name", "Medium");
        mediumData.put("value", mediumTopics.size());
        pieData.add(mediumData);
        
        Map<String, Object> hardData = new HashMap<>();
        hardData.put("name", "Hard");
        hardData.put("value", hardTopics.size());
        pieData.add(hardData);
        
        distributionData.put("pieData", pieData);
        distributionData.put("total", easyTopics.size() + mediumTopics.size() + hardTopics.size());
        
        return distributionData;
    }
    
    @Override
    public Map<String, Object> getTopicStatsByTeacher(Long teacherId) {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<Topic> totalQuery = new QueryWrapper<>();
        totalQuery.eq("teacher_id", teacherId).eq("deleted", 0);
        long totalTopics = count(totalQuery);
        stats.put("totalTopics", totalTopics);
        
        QueryWrapper<Topic> activeQuery = new QueryWrapper<>();
        activeQuery.eq("teacher_id", teacherId).eq("status", "active").eq("deleted", 0);
        long activeTopics = count(activeQuery);
        
        QueryWrapper<Topic> completedQuery = new QueryWrapper<>();
        completedQuery.eq("teacher_id", teacherId).eq("status", "completed").eq("deleted", 0);
        long completedTopics = count(completedQuery);
        
        QueryWrapper<Topic> pausedQuery = new QueryWrapper<>();
        pausedQuery.eq("teacher_id", teacherId).eq("status", "paused").eq("deleted", 0);
        long pausedTopics = count(pausedQuery);
        
        stats.put("activeTopics", activeTopics);
        stats.put("completedTopics", completedTopics);
        stats.put("pausedTopics", pausedTopics);
        
        QueryWrapper<Topic> easyQuery = new QueryWrapper<>();
        easyQuery.eq("teacher_id", teacherId).eq("difficulty", "easy").eq("deleted", 0);
        long easyTopics = count(easyQuery);
        
        QueryWrapper<Topic> mediumQuery = new QueryWrapper<>();
        mediumQuery.eq("teacher_id", teacherId).eq("difficulty", "medium").eq("deleted", 0);
        long mediumTopics = count(mediumQuery);
        
        QueryWrapper<Topic> hardQuery = new QueryWrapper<>();
        hardQuery.eq("teacher_id", teacherId).eq("difficulty", "hard").eq("deleted", 0);
        long hardTopics = count(hardQuery);
        
        stats.put("easyTopics", easyTopics);
        stats.put("mediumTopics", mediumTopics);
        stats.put("hardTopics", hardTopics);
        
        QueryWrapper<Topic> studentsQuery = new QueryWrapper<>();
        studentsQuery.eq("teacher_id", teacherId).eq("deleted", 0);
        List<Topic> teacherTopics = list(studentsQuery);
        int totalStudents = teacherTopics.stream()
                .mapToInt(topic -> topic.getSelectedCount() != null ? topic.getSelectedCount() : 0)
                .sum();
        stats.put("totalStudents", totalStudents);
        
        double avgRating = teacherTopics.stream()
                .filter(topic -> topic.getRating() != null && topic.getRating() > 0)
                .mapToDouble(Topic::getRating)
                .average()
                .orElse(0.0);
        stats.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
        
        int totalViews = teacherTopics.stream()
                .mapToInt(topic -> topic.getViewCount() != null ? topic.getViewCount() : 0)
                .sum();
        stats.put("totalViews", totalViews);
        
        return stats;
    }
    
    private long getCurrentMonthTopicCount() {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }
    
    private long getLastMonthTopicCount() {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }

    private static int clampListLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    @Override
    public List<Map<String, Object>> getRecentTopics(int limit) {
        int safe = clampListLimit(limit);
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("create_time")
                   .last("LIMIT " + safe);
        List<Topic> topics = list(queryWrapper);
        return topics.stream().map(topic -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", topic.getId());
            map.put("title", topic.getTitle());
            map.put("teacher_id", topic.getTeacherId());
            map.put("teacher_name", topic.getTeacherName());
            map.put("difficulty", topic.getDifficulty());
            map.put("create_time", topic.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }
}
