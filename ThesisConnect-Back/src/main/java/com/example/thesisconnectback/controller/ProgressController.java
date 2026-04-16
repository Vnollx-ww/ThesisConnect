package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Progress;
import com.example.thesisconnectback.service.ProgressService;
import com.example.thesisconnectback.service.SelectionService;
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

    @Autowired
    private SelectionService selectionService;

    /**
     * 更新进度
     */
    @PostMapping("/update")
    public Result<Void> updateProgress(@RequestBody Map<String, Object> progressForm, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if ("student".equals(role)) {
            return Result.forbidden("进度由指导教师根据管理员预定链路推进，不可自行填报");
        }
        return Result.forbidden("请使用选题进度链路，不再使用本接口");
    }

    /**
     * 审核进度
     */
    @PostMapping("/review")
    public Result<Void> reviewProgress(@RequestBody Map<String, Object> reviewForm, HttpServletRequest request) {
        try {
            // 检查权限（只有教师和管理员可以审核进度）
            String role = (String) request.getAttribute("role");
            if (!"teacher".equals(role) && !"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Long id = Long.valueOf(reviewForm.get("id").toString());
            String status = (String) reviewForm.get("status");
            String rejectReason = (String) reviewForm.get("rejectReason");

            boolean success = progressService.reviewProgress(id, status, rejectReason);
            if (success) {
                return Result.success("审核成功");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("审核进度失败：", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 添加里程碑
     */
    @PostMapping("/milestone")
    public Result<Void> addMilestone(@RequestBody Map<String, Object> milestoneForm, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if ("student".equals(role)) {
            return Result.forbidden("里程碑由管理员在进度链路中配置，不可自行添加");
        }
        return Result.forbidden("权限不足");
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
            
            // 允许管理员、教师和学生本人查看进度
            if (!"admin".equals(role) && !"teacher".equals(role) && !userId.equals(studentId)) {
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
        String role = (String) request.getAttribute("role");
        if ("student".equals(role)) {
            return Result.forbidden("进度节点由指导教师推进，不可自行修改");
        }
        return Result.forbidden("请使用选题进度链路接口");
    }

    /**
     * 预定进度链路展示（节点状态由教师/管理员推进）
     */
    @GetMapping("/chain-view/{selectionId}")
    public Result<Map<String, Object>> getChainView(@PathVariable Long selectionId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");
            Map<String, Object> view = selectionService.getProgressChainView(selectionId, userId, role);
            if (view == null) {
                return Result.forbidden("权限不足");
            }
            if (Boolean.TRUE.equals(view.get("_notFound"))) {
                return Result.notFound("选题记录不存在");
            }
            view.remove("_notFound");
            return Result.success(view);
        } catch (Exception e) {
            log.error("获取进度链路失败：", e);
            return Result.error("获取进度链路失败");
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
