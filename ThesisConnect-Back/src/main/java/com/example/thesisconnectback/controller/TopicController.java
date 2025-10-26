package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.service.TopicService;
import com.example.thesisconnectback.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课题管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/topics")
@CrossOrigin
public class TopicController {

    @Autowired
    private TopicService topicService;
    @Autowired
    private UserService userService;

    /**
     * 获取课题列表（分页）
     */
    @GetMapping
    public Result<Page<Topic>> getTopicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Topic> pageParam = new Page<>(page, size);
            QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();

            // 专业筛选
            if (major != null && !major.isEmpty()) {
                queryWrapper.eq("major", major);
            }

            // 难度筛选
            if (difficulty != null && !difficulty.isEmpty()) {
                queryWrapper.eq("difficulty", difficulty);
            }

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("title", keyword)
                        .or()
                        .like("description", keyword)
                );
            }

            queryWrapper.orderByDesc("create_time");
            Page<Topic> result = topicService.page(pageParam, queryWrapper);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取课题列表失败：", e);
            return Result.error("获取课题列表失败");
        }
    }

    /**
     * 获取课题详情
     */
    @GetMapping("/{id}")
    public Result<Topic> getTopicById(@PathVariable Long id) {
        try {
            Topic topic = topicService.getById(id);
            if (topic != null) {
                // 增加浏览量
                topicService.incrementViewCount(id);
                return Result.success(topic);
            } else {
                return Result.notFound("课题不存在");
            }
        } catch (Exception e) {
            log.error("获取课题详情失败：", e);
            return Result.error("获取课题详情失败");
        }
    }

    /**
     * 创建课题
     */
    @PostMapping
    public Result<Topic> createTopic(@RequestBody Topic topic, HttpServletRequest request) {
        try {
            // 检查权限（只有教师和管理员可以创建课题）
            String role = (String) request.getAttribute("role");
            if (!"teacher".equals(role) && !"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Long userId = (Long) request.getAttribute("userId");
            topic.setTeacherId(userId);
            topic.setTeacherName((String) request.getAttribute("username"));
            boolean success = topicService.publishTopic(topic);
            if (success) {
                return Result.success("课题发布成功", topic);
            } else {
                return Result.error("课题发布失败");
            }
        } catch (Exception e) {
            log.error("创建课题失败：", e);
            return Result.error("创建课题失败");
        }
    }

    /**
     * 更新课题信息
     */
    @PutMapping("/{id}")
    public Result<Topic> updateTopic(@PathVariable Long id, @RequestBody Topic topic, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            Topic existingTopic = topicService.getById(id);
            if (existingTopic == null) {
                return Result.notFound("课题不存在");
            }

            // 只有课题创建者或管理员可以修改
            if (!"admin".equals(role) && !userId.equals(existingTopic.getTeacherId())) {
                return Result.forbidden("权限不足");
            }

            // 更新课题信息
            existingTopic.setTitle(topic.getTitle());
            existingTopic.setDescription(topic.getDescription());
            existingTopic.setMajor(topic.getMajor());
            existingTopic.setDifficulty(topic.getDifficulty());
            existingTopic.setMaxStudents(topic.getMaxStudents());
            existingTopic.setRequirements(topic.getRequirements());
            existingTopic.setExpectedOutcome(topic.getExpectedOutcome());
            existingTopic.setDeadline(topic.getDeadline());
            existingTopic.setStatus(topic.getStatus());

            boolean success = topicService.updateTopic(existingTopic);
            if (success) {
                return Result.success("课题信息更新成功", existingTopic);
            } else {
                return Result.error("课题信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新课题信息失败：", e);
            return Result.error("更新课题信息失败");
        }
    }

    /**
     * 删除课题
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTopic(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            Topic topic = topicService.getById(id);
            if (topic == null) {
                return Result.notFound("课题不存在");
            }

            // 只有课题创建者或管理员可以删除
            if (!"admin".equals(role) && !userId.equals(topic.getTeacherId())) {
                return Result.forbidden("权限不足");
            }

            boolean success = topicService.deleteTopic(id);
            if (success) {
                return Result.success("课题删除成功");
            } else {
                return Result.error("课题删除失败");
            }
        } catch (Exception e) {
            log.error("删除课题失败：", e);
            return Result.error("删除课题失败");
        }
    }

    /**
     * 根据教师ID获取课题列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Topic>> getTopicsByTeacher(@PathVariable Long teacherId, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            if (!"admin".equals(role) && !userId.equals(teacherId)) {
                return Result.forbidden("权限不足");
            }

            List<Topic> topics = topicService.findByTeacherId(teacherId);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("获取教师课题列表失败：", e);
            return Result.error("获取教师课题列表失败");
        }
    }

    /**
     * 根据专业获取课题列表
     */
    @GetMapping("/major/{major}")
    public Result<List<Topic>> getTopicsByMajor(@PathVariable String major) {
        try {
            List<Topic> topics = topicService.findByMajor(major);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("获取专业课题列表失败：", e);
            return Result.error("获取专业课题列表失败");
        }
    }

    /**
     * 根据难度获取课题列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Topic>> getTopicsByDifficulty(@PathVariable String difficulty) {
        try {
            List<Topic> topics = topicService.findByDifficulty(difficulty);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("获取难度课题列表失败：", e);
            return Result.error("获取难度课题列表失败");
        }
    }

    /**
     * 搜索课题
     */
    @GetMapping("/search")
    public Result<List<Topic>> searchTopics(@RequestParam String keyword) {
        try {
            List<Topic> topics = topicService.searchByKeyword(keyword);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("搜索课题失败：", e);
            return Result.error("搜索课题失败");
        }
    }

    /**
     * 获取热门课题
     */
    @GetMapping("/popular")
    public Result<List<Topic>> getPopularTopics(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Topic> topics = topicService.getPopularTopics(limit);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("获取热门课题失败：", e);
            return Result.error("获取热门课题失败");
        }
    }

    /**
     * 获取课题统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getTopicStats(HttpServletRequest request) {
        try {
            // 检查权限（管理员和教师都可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role) && !"teacher".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = topicService.getTopicStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取课题统计信息失败：", e);
            return Result.error("获取课题统计信息失败");
        }
    }
    
    /**
     * 获取指定教师的课题统计信息
     */
    @GetMapping("/teacher/{teacherId}/stats")
    public Result<Map<String, Object>> getTopicStatsByTeacher(@PathVariable Long teacherId, HttpServletRequest request) {
        try {
            // 检查权限（管理员和教师本人可以查看）
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            
            if (!"admin".equals(role) && !userId.equals(teacherId)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = topicService.getTopicStatsByTeacher(teacherId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取教师课题统计信息失败：", e);
            return Result.error("获取教师课题统计信息失败");
        }
    }
}
