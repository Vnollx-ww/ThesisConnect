package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.service.DocumentService;
import com.example.thesisconnectback.service.ProgressService;
import com.example.thesisconnectback.service.SelectionService;
import com.example.thesisconnectback.service.TopicService;
import com.example.thesisconnectback.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ProgressService progressService;

    /**
     * 获取系统总览统计
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverviewStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = new HashMap<>();
            
            // 用户统计
            Map<String, Object> userStats = userService.getUserStats();
            stats.putAll(userStats);
            
            // 课题统计
            Map<String, Object> topicStats = topicService.getTopicStats();
            stats.putAll(topicStats);
            
            // 选题统计
            Map<String, Object> selectionStats = selectionService.getSelectionStats();
            stats.putAll(selectionStats);
            
            // 文档统计
            Map<String, Object> documentStats = documentService.getDocumentStats();
            stats.putAll(documentStats);
            
            // 进度统计
            Map<String, Object> progressStats = progressService.getProgressStats();
            stats.putAll(progressStats);

            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取系统统计信息失败：", e);
            return Result.error("获取系统统计信息失败");
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = userService.getUserStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败：", e);
            return Result.error("获取用户统计信息失败");
        }
    }

    /**
     * 获取课题统计
     */
    @GetMapping("/topics")
    public Result<Map<String, Object>> getTopicStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
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
     * 获取选题统计
     */
    @GetMapping("/selections")
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

    /**
     * 获取文档统计
     */
    @GetMapping("/documents")
    public Result<Map<String, Object>> getDocumentStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = documentService.getDocumentStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取文档统计信息失败：", e);
            return Result.error("获取文档统计信息失败");
        }
    }

    /**
     * 获取进度统计
     */
    @GetMapping("/progress")
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
