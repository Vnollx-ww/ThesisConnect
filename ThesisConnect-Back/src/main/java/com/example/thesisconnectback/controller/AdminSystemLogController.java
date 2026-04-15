package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.SystemLog;
import com.example.thesisconnectback.service.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 系统操作日志（管理员）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system-logs")
@CrossOrigin
public class AdminSystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    @GetMapping
    public Result<Page<SystemLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden("权限不足");
        }
        Page<SystemLog> p = new Page<>(page, size);
        QueryWrapper<SystemLog> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        return Result.success(systemLogService.page(p, qw));
    }
}
