package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.service.SelectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam(required = false) Long topicId) {
        try {
            Page<Selection> pageParam = new Page<>(page, size);
            QueryWrapper<Selection> queryWrapper = new QueryWrapper<>();

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 学生ID筛选
            if (studentId != null) {
                queryWrapper.eq("student_id", studentId);
            }

            // 教师ID筛选
            if (teacherId != null) {
                queryWrapper.eq("teacher_id", teacherId);
            }

            // 课题ID筛选
            if (topicId != null) {
                queryWrapper.eq("topic_id", topicId);
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
    public Result<Selection> getSelectionById(@PathVariable Long id) {
        try {
            Selection selection = selectionService.getById(id);
            if (selection != null) {
                return Result.success(selection);
            } else {
                return Result.notFound("选题记录不存在");
            }
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

            // 检查学生是否已选择课题
            if (selectionService.hasSelectedTopic(userId)) {
                return Result.error("您已经选择过课题了");
            }

            // 检查课题是否已满员
            if (selectionService.isTopicFull(topicId)) {
                return Result.error("该课题已满员");
            }

            boolean success = selectionService.selectTopic(userId, topicId);
            if (success) {
                return Result.success("选题成功");
            } else {
                // 再次检查是否是因为被拒绝
                try {
                     // 反射调用mapper检查，或者简单返回通用错误
                     // 这里简单处理，如果返回false且不是以上原因，可能是被拒绝了
                     // 更好的方式是在service抛出异常或返回具体错误码，这里暂时通过Result返回通用失败
                     // 实际上我们可以优化service让它抛出异常
                     return Result.error("选题失败，您可能已被该课题拒绝或不满足其他条件");
                } catch (Exception e) {
                    return Result.error("选题失败");
                }
            }
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

            // 只有待审核或已拒绝状态可以取消/删除
            if (!"pending".equals(selection.getStatus()) && !"rejected".equals(selection.getStatus())) {
                return Result.error("只有待审核或已拒绝状态的选题可以取消或删除");
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

            boolean success = selectionService.reviewSelection(id, status, comment);
            if (success) {
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
            
            // 检查权限：只有申请人本人可以确认
            if (!selection.getStudentId().equals(userId)) {
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
    public Result<List<Selection>> getSelectionsByTopic(@PathVariable Long topicId) {
        try {
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
