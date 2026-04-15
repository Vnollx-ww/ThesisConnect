package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 系统配置（管理员）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system-config")
@CrossOrigin
public class AdminSystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping
    public Result<Map<String, String>> getConfig(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden("权限不足");
        }
        return Result.success(systemConfigService.getAllConfig());
    }

    @PutMapping
    public Result<Void> saveConfig(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden("权限不足");
        }
        try {
            systemConfigService.saveConfig(body);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存系统配置失败", e);
            return Result.error("保存失败");
        }
    }
}
