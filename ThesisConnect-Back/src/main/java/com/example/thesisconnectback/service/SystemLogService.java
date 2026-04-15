package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.SystemLog;

import jakarta.servlet.http.HttpServletRequest;

public interface SystemLogService extends IService<SystemLog> {

    void saveLog(Long userId, String username, String operation, String description, HttpServletRequest request);
}
