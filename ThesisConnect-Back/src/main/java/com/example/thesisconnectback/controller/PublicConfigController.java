package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 无需登录的公开配置（选题规则展示）
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin
public class PublicConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/selection-rules")
    public Result<Map<String, String>> selectionRules() {
        return Result.success(systemConfigService.getPublicSelectionRules());
    }
}
