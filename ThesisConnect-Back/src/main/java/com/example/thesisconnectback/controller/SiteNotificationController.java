package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.SiteNotification;
import com.example.thesisconnectback.service.SiteNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 站内通知与待办汇总（与邮件互补）
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class SiteNotificationController {

    @Autowired
    private SiteNotificationService siteNotificationService;

    @GetMapping
    public Result<Page<SiteNotification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Integer readFlag,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.forbidden("权限不足");
        }
        Boolean read = readFlag == null ? null : (readFlag == 1);
        return Result.success(siteNotificationService.pageForUser(userId, page, size, bizType, read));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.forbidden("权限不足");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("count", siteNotificationService.unreadCount(userId));
        return Result.success(m);
    }

    /** 待办：教师待审数、学生待确认数、未读通知数 */
    @GetMapping("/pending-summary")
    public Result<Map<String, Long>> pendingSummary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (userId == null) {
            return Result.forbidden("权限不足");
        }
        return Result.success(siteNotificationService.pendingSummary(userId, role));
    }

    @PatchMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.forbidden("权限不足");
        }
        if (siteNotificationService.markRead(userId, id)) {
            return Result.success("已标记已读");
        }
        return Result.notFound("通知不存在");
    }

    @PostMapping("/read-all")
    public Result<Map<String, Object>> markAllRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.forbidden("权限不足");
        }
        int n = siteNotificationService.markAllRead(userId);
        Map<String, Object> m = new HashMap<>();
        m.put("updated", n);
        return Result.success(m);
    }
}
