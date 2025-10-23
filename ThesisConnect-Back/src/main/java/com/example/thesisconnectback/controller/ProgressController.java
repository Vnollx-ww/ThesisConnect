package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Progress;
import com.example.thesisconnectback.service.ProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 进度管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/progress")
@CrossOrigin
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    /**
     * 更新进度
     */
    @PostMapping("/update")
    public Result<Void> updateProgress(@RequestBody Map<String, Object> progressForm, HttpServletRequest request) {
        try {
            // 检查权限（只有学生可以更新进度）
            String role = (String) request.getAttribute("role");
            if (!"student".equals(role)) {
                return Result.forbidden("只有学生可以更新进度");
            }

            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            Long selectionId = Long.valueOf(progressForm.get("selectionId").toString());
            String milestoneTitle = (String) progressForm.get("milestoneTitle");
            String milestoneStatus = (String) progressForm.get("milestoneStatus");
            Integer percentage = Integer.valueOf(progressForm.get("percentage").toString());
            String description = (String) progressForm.get("description");
            String problems = (String) progressForm.get("problems");

            boolean success = progressService.updateProgress(selectionId, milestoneTitle, milestoneStatus, percentage, description, problems, userId, username);
            if (success) {
                return Result.success("进度更新成功");
            } else {
                return Result.error("进度更新失败");
            }
        } catch (Exception e) {
            log.error("更新进度失败：", e);
            return Result.error("更新进度失败：" + e.getMessage());
        }
    }

    /**
     * 添加里程碑
     */
    @PostMapping("/milestone")
    public Result<Void> addMilestone(@RequestBody Map<String, Object> milestoneForm, HttpServletRequest request) {
        try {
            // 检查权限（只有学生可以添加里程碑）
            String role = (String) request.getAttribute("role");
            if (!"student".equals(role)) {
                return Result.forbidden("只有学生可以添加里程碑");
            }

            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            Long selectionId = Long.valueOf(milestoneForm.get("selectionId").toString());
            String title = (String) milestoneForm.get("title");
            String description = (String) milestoneForm.get("description");
            String status = (String) milestoneForm.get("status");

            boolean success = progressService.addMilestone(selectionId, title, description, status, userId, username);
            if (success) {
                return Result.success("里程碑添加成功");
            } else {
                return Result.error("里程碑添加失败");
            }
        } catch (Exception e) {
            log.error("添加里程碑失败：", e);
            return Result.error("添加里程碑失败：" + e.getMessage());
        }
    }

    /**
     * 根据选题ID获取进度记录
     */
    @GetMapping("/selection/{selectionId}")
    public Result<List<Progress>> getProgressBySelection(@PathVariable Long selectionId) {
        try {
            List<Progress> progressList = progressService.getProgressBySelectionId(selectionId);
            return Result.success(progressList);
        } catch (Exception e) {
            log.error("获取进度记录失败：", e);
            return Result.error("获取进度记录失败");
        }
    }

    /**
     * 根据学生ID获取进度记录
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Progress>> getProgressByStudent(@PathVariable Long studentId, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            
            if (!"admin".equals(role) && !userId.equals(studentId)) {
                return Result.forbidden("权限不足");
            }

            List<Progress> progressList = progressService.getProgressByStudentId(studentId);
            return Result.success(progressList);
        } catch (Exception e) {
            log.error("获取进度记录失败：", e);
            return Result.error("获取进度记录失败");
        }
    }

    /**
     * 根据课题ID获取进度记录
     */
    @GetMapping("/topic/{topicId}")
    public Result<List<Progress>> getProgressByTopic(@PathVariable Long topicId) {
        try {
            List<Progress> progressList = progressService.getProgressByTopicId(topicId);
            return Result.success(progressList);
        } catch (Exception e) {
            log.error("获取进度记录失败：", e);
            return Result.error("获取进度记录失败");
        }
    }

    /**
     * 获取最新进度
     */
    @GetMapping("/latest/{selectionId}")
    public Result<Progress> getLatestProgress(@PathVariable Long selectionId) {
        try {
            Progress progress = progressService.getLatestProgress(selectionId);
            if (progress != null) {
                return Result.success(progress);
            } else {
                return Result.notFound("暂无进度记录");
            }
        } catch (Exception e) {
            log.error("获取最新进度失败：", e);
            return Result.error("获取最新进度失败");
        }
    }

    /**
     * 更新里程碑状态
     */
    @PutMapping("/milestone/status")
    public Result<Void> updateMilestoneStatus(@RequestBody Map<String, Object> updateData, HttpServletRequest request) {
        try {
            // 检查权限（只有学生可以更新里程碑状态）
            String role = (String) request.getAttribute("role");
            if (!"student".equals(role)) {
                return Result.forbidden("只有学生可以更新里程碑状态");
            }

            Long milestoneId = Long.valueOf(updateData.get("id").toString());
            String milestoneStatus = (String) updateData.get("milestoneStatus");

            boolean success = progressService.updateMilestoneStatus(milestoneId, milestoneStatus);
            if (success) {
                return Result.success("里程碑状态更新成功");
            } else {
                return Result.error("里程碑状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新里程碑状态失败：", e);
            return Result.error("更新里程碑状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取进度统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getProgressStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = progressService.getProgressStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取进度统计信息失败：", e);
            return Result.error("获取进度统计信息失败");
        }
    }
}
