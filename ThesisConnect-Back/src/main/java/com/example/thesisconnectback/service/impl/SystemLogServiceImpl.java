package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.SystemLog;
import com.example.thesisconnectback.mapper.SystemLogMapper;
import com.example.thesisconnectback.service.SystemLogService;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    public void saveLog(Long userId, String username, String operation, String description, HttpServletRequest request) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setDescription(description);
        if (request != null) {
            log.setMethod(request.getMethod() + " " + request.getRequestURI());
            log.setIp(request.getRemoteAddr());
            String ua = request.getHeader("User-Agent");
            log.setUserAgent(ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua);
        }
        log.setTime(0L);
        save(log);
    }
}
