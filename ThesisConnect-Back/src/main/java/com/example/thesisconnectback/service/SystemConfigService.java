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

    /** 是否允许跨专业选题；未配置默认 true */
    boolean isAllowCrossMajor();

    /** 同一学生对同一教师（名下课题）最多同时保留的非 rejected 申请数；未配置默认 99 */
    int getMaxSelectionsPerTeacherPerStudent();

    Map<String, String> getPublicSelectionRules();
}
