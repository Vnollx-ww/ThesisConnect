package com.example.thesisconnectback.interceptor;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.service.TokenBlacklistService;
import com.example.thesisconnectback.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理跨域预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取token
        String authHeader = request.getHeader(jwtUtil.getHeader());
        String token = jwtUtil.getTokenFromHeader(authHeader);

        if (token == null) {
            writeErrorResponse(response, "未提供认证token");
            return false;
        }

        if (tokenBlacklistService.isBlacklisted(token)) {
            writeErrorResponse(response, "登录已失效，请重新登录");
            return false;
        }

        try {
            // 验证token
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                writeErrorResponse(response, "无效的认证token");
                return false;
            }

            // 将用户信息存储到请求属性中
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            return true;
        } catch (Exception e) {
            log.error("JWT认证失败：", e);
            writeErrorResponse(response, "认证失败");
            return false;
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Result<Void> result = Result.unauthorized(message);
        String json = objectMapper.writeValueAsString(result);
        
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
        writer.close();
    }
}
