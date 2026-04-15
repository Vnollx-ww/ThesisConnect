package com.example.thesisconnectback.service;

import java.util.Map;

/**
 * 系统配置（选题规则等）
 */
public interface SystemConfigService {

    Map<String, String> getAllConfig();

    void saveConfig(Map<String, String> config);

    /** 是否在选题开放时间内（未配置起止时间则视为始终开放） */
    boolean isWithinSelectionPeriod();

    int getMaxSelectionsPerStudent();

    Map<String, String> getPublicSelectionRules();
}
