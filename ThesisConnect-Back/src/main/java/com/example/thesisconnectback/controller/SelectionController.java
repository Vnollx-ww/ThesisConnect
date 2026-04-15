package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.exception.BusinessException;
import com.example.thesisconnectback.mail.MailNotificationService;
import com.example.thesisconnectback.service.UserService;
import com.example.thesisconnectback.service.SelectionService;
import com.example.thesisconnectback.service.SystemLogService;
import com.example.thesisconnectback.service.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 选题管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/selections")
@CrossOrigin
public class SelectionController {

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailNotificationService mailNotificationService;

    /**
     * 获取选题列表（分页）
     */
    @GetMapping
    public Result<Page<Selection>> getSelectionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long topicId,
            HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            if (role == null || userId == null) {
                return Result.forbidden("权限不足");
            }

            Page<Selection> pageParam = new Page<>(page, size);
            QueryWrapper<Selection> queryWrapper = new QueryWrapper<>();

            // 非管理员只能查看与本人相关的选题，忽略越权筛选参数
            if ("student".equals(role)) {
                queryWrapper.eq("student_id", userId);
            } else if ("teacher".equals(role)) {
                queryWrapper.eq("teacher_id", userId);
            } else if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 学生ID筛选（仅管理员）
            if ("admin".equals(role) && studentId != null) {
                queryWrapper.eq("student_id", studentId);
            }

            // 教师ID筛选（仅管理员）
            if ("admin".equals(role) && teacherId != null) {
                queryWrapper.eq("teacher_id", teacherId);
            }

            // 课题ID筛选
            if (topicId != null) {
                if ("admin".equals(role)) {
                    queryWrapper.eq("topic_id", topicId);
                } else if ("teacher".equals(role)) {
                    Topic topic = topicService.getById(topicId);
                    if (topic == null || !userId.equals(topic.getTeacherId())) {
                        return Result.forbidden("权限不足");
                    }
                    queryWrapper.eq("topic_id", topicId);
                } else if ("student".equals(role)) {
                    queryWrapper.eq("topic_id", topicId);
                }
            }

            queryWrapper.orderByDesc("create_time");
            Page<Selection> result = selectionService.page(pageParam, queryWrapper);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取选题列表失败：", e);
            return Result.error("获取选题列表失败");
        }
    }

    /**
     * 获取选题详情
     */
    @GetMapping("/{id}")
    public Result<Selection> getSelectionById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            Selection selection = selectionService.getById(id);
            if (selection == null) {
                return Result.notFound("选题记录不存在");
            }
            if (!"admin".equals(role)) {
                boolean allowed = userId != null && (userId.equals(selection.getStudentId()) || userId.equals(selection.getTeacherId()));
                if (!allowed) {
                    return Result.forbidden("权限不足");
                }
            }
            return Result.success(selection);
        } catch (Exception e) {
            log.error("获取选题详情失败：", e);
            return Result.error("获取选题详情失败");
        }
    }

    /**
     * 学生选择课题
     */
    @PostMapping
    public Result<Selection> selectTopic(@RequestBody Map<String, Long> selectionForm, HttpServletRequest request) {
        try {
            // 检查权限（只有学生可以选择课题）
            String role = (String) request.getAttribute("role");
            if (!"student".equals(role)) {
                return Result.forbidden("只有学生可以选择课题");
            }

            Long userId = (Long) request.getAttribute("userId");
            Long topicId = selectionForm.get("topicId");

            if (topicId == null) {
                return Result.badRequest("课题ID不能为空");
            }

            selectionService.validateSelectTopicRequest(userId, topicId);

            Selection created = selectionService.selectTopic(userId, topicId);
            try {
                Topic topic = topicService.getById(topicId);
                if (topic != null) {
                    User teacher = userService.getById(topic.getTeacherId());
                    User student = userService.getById(userId);
                    if (teacher != null && StringUtils.hasText(teacher.getEmail())) {
                        mailNotificationService.sendTeacherNewSelectionApplication(
                                teacher.getEmail(),
                                teacher.getRealName() != null ? teacher.getRealName() : teacher.getUsername(),
                                student != null && student.getRealName() != null ? student.getRealName() : "",
                                topic.getTitle(),
                                LocalDateTime.now());
                    }
                }
            } catch (Exception e) {
                log.warn("选题成功但通知教师邮件失败 topicId={} studentId={}: {}", topicId, userId, e.getMessage());
            }
            return Result.success("选题成功", created);
        } catch (BusinessException e) {
            return Result.error(e.getCode() != null ? e.getCode() : 400, e.getMessage());
        } catch (Exception e) {
            log.error("选择课题失败：", e);
            return Result.error("选择课题失败");
        }
    }

    /**
     * 取消选择课题
     */
    @DeleteMapping("/{id}")
    public Result<Void> cancelSelection(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            Selection selection = selectionService.getById(id);
            if (selection == null) {
                return Result.notFound("选题记录不存在");
            }

            // 只有学生本人或管理员可以取消选择
            if (!"admin".equals(role) && !userId.equals(selection.getStudentId())) {
                return Result.forbidden("权限不足");
            }

            // 只有待审核、已通过待确认或已拒绝状态可以取消/删除
            if (!"pending".equals(selection.getStatus()) && !"approved".equals(selection.getStatus()) && !"rejected".equals(selection.getStatus())) {
                return Result.error("只有待审核、已通过待确认或已拒绝状态的选题可以取消或删除");
            }

            boolean success = selectionService.cancelSelection(id);
            if (success) {
                return Result.success("操作成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("取消选题失败：", e);
            return Result.error("取消选题失败");
        }
    }

    /**
     * 审核选题
     */
    @PostMapping("/{id}/review")
    public Result<Void> reviewSelection(@PathVariable Long id, @RequestBody Map<String, String> reviewForm, HttpServletRequest request) {
        try {
            // 检查权限（只有教师和管理员可以审核）
            String role = (String) request.getAttribute("role");
            if (!"teacher".equals(role) && !"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            String status = reviewForm.get("status");
            String comment = reviewForm.get("comment");

            if (status == null || (!"approved".equals(status) && !"rejected".equals(status))) {
                return Result.badRequest("审核状态无效");
            }

            Long userId = (Long) request.getAttribute("userId");
            Selection selection = selectionService.getById(id);
            if (selection == null) {
                return Result.notFound("选题记录不存在");
            }
            if ("teacher".equals(role) && (userId == null || !userId.equals(selection.getTeacherId()))) {
                return Result.forbidden("只能审核本人课题的选题申请");
            }

            boolean success = selectionService.reviewSelection(id, status, comment);
            if (success) {
                try {
                    systemLogService.saveLog((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                            "REVIEW_SELECTION", "审核选题 id=" + id + " -> " + status, request);
                } catch (Exception e) {
                    log.warn("审核选题后写系统日志失败 selectionId={}: {}", id, e.getMessage());
                }
                try {
                    Selection sel = selectionService.getById(id);
                    if (sel != null) {
                        User student = userService.getById(sel.getStudentId());
                        User teacher = userService.getById(sel.getTeacherId());
                        String tName = teacher != null && teacher.getRealName() != null ? teacher.getRealName() : "";
                        if (student != null && StringUtils.hasText(student.getEmail())) {
                            if ("approved".equals(status)) {
                                mailNotificationService.sendStudentSelectionApproved(
                                        student.getEmail(),
                                        student.getRealName() != null ? student.getRealName() : student.getUsername(),
                                        sel.getTopicTitle(),
                                        tName,
                                        comment);
                            } else {
                                mailNotificationService.sendStudentSelectionRejected(
                                        student.getEmail(),
                                        student.getRealName() != null ? student.getRealName() : student.getUsername(),
                                        sel.getTopicTitle(),
                                        tName,
                                        comment);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("审核选题后通知学生邮件失败 selectionId={}: {}", id, e.getMessage());
                }
                return Result.success("审核完成");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("审核选题失败：", e);
            return Result.error("审核选题失败");
        }
    }

    /**
     * 学生确认申请
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmApplication(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            Selection selection = selectionService.getById(id);
            if (selection == null) {
                return Result.notFound("选题记录不存在");
            }
            
            if (userId == null || !userId.equals(selection.getStudentId())) {
                return Result.forbidden("只能确认自己的申请");
            }
            
            // 只有审核通过的申请可以确认
            if (!"approved".equals(selection.getStatus())) {
                return Result.error("只能确认已审核通过的申请");
            }
            
            // 将状态从approved改为confirmed
            boolean success = selectionService.updateSelectionStatus(id, "confirmed");
            if (success) {
                return Result.success("确认成功");
            } else {
                return Result.error("确认失败");
            }
        } catch (Exception e) {
            log.error("确认申请失败：", e);
            return Result.error("确认申请失败");
        }
    }
    
    /**
     * 更新进度
     */
    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> progressForm, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            Selection selection = selectionService.getById(id);
            if (selection == null) {
                return Result.notFound("选题记录不存在");
            }

            // 只有学生本人可以更新进度
            if (!userId.equals(selection.getStudentId())) {
                return Result.forbidden("权限不足");
            }

            Integer progress = (Integer) progressForm.get("progress");
            String description = (String) progressForm.get("description");
            String problems = (String) progressForm.get("problems");

            if (progress == null || progress < 0 || progress > 100) {
                return Result.badRequest("进度值无效");
            }

            boolean success = selectionService.updateProgress(id, progress, description, problems);
            if (success) {
                return Result.success("进度更新成功");
            } else {
                return Result.error("进度更新失败");
            }
        } catch (Exception e) {
            log.error("更新进度失败：", e);
            return Result.error("更新进度失败");
        }
    }

    /**
     * 评价打分
     */
    @PostMapping("/{id}/grade")
    public Result<Void> gradeSelection(@PathVariable Long id, @RequestBody Map<String, String> gradeForm, HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            if (!"teacher".equals(role) && !"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            String grade = gradeForm.get("grade");
            String evaluation = gradeForm.get("evaluation");

            if (grade == null || grade.isEmpty()) {
                return Result.badRequest("成绩不能为空");
            }

            Long userId = (Long) request.getAttribute("userId");
            Selection sel = selectionService.getById(id);
            if (sel == null) {
                return Result.notFound("选题记录不存在");
            }
            if (!"admin".equals(role) && (userId == null || !userId.equals(sel.getTeacherId()))) {
                return Result.forbidden("只能评价本人指导的选题");
            }

            boolean success = selectionService.gradeSelection(id, grade, evaluation);
            if (success) {
                return Result.success("评价打分成功");
            } else {
                return Result.error("评价打分失败");
            }
        } catch (Exception e) {
            log.error("评价打分失败：", e);
            return Result.error("评价打分失败");
        }
    }

    /**
     * 根据学生ID获取选题记录
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Selection>> getSelectionsByStudent(@PathVariable Long studentId, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            if (!"admin".equals(role) && !userId.equals(studentId)) {
                return Result.forbidden("权限不足");
            }

            List<Selection> selections = selectionService.findByStudentId(studentId);
            return Result.success(selections);
        } catch (Exception e) {
            log.error("获取学生选题记录失败：", e);
            return Result.error("获取学生选题记录失败");
        }
    }

    /**
     * 根据教师ID获取选题记录
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Selection>> getSelectionsByTeacher(@PathVariable Long teacherId, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");

            if (!"admin".equals(role) && !userId.equals(teacherId)) {
                return Result.forbidden("权限不足");
            }

            List<Selection> selections = selectionService.findByTeacherId(teacherId);
            return Result.success(selections);
        } catch (Exception e) {
            log.error("获取教师选题记录失败：", e);
            return Result.error("获取教师选题记录失败");
        }
    }

    /**
     * 根据课题ID获取选题记录
     */
    @GetMapping("/topic/{topicId}")
    public Result<List<Selection>> getSelectionsByTopic(@PathVariable Long topicId, HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            Topic topic = topicService.getById(topicId);
            if (topic == null) {
                return Result.notFound("课题不存在");
            }
            if (!"admin".equals(role)) {
                if (!"teacher".equals(role) || userId == null || !userId.equals(topic.getTeacherId())) {
                    return Result.forbidden("权限不足");
                }
            }
            List<Selection> selections = selectionService.findByTopicId(topicId);
            return Result.success(selections);
        } catch (Exception e) {
            log.error("获取课题选题记录失败：", e);
            return Result.error("获取课题选题记录失败");
        }
    }

    /**
     * 获取选题统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getSelectionStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = selectionService.getSelectionStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取选题统计信息失败：", e);
            return Result.error("获取选题统计信息失败");
        }
    }
}
