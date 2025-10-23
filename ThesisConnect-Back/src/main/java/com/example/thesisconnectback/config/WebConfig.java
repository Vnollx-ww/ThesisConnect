package com.example.thesisconnectback.config;

import com.example.thesisconnectback.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login",           // 登录接口
                        "/api/auth/register",        // 注册接口
                        "/api/public/**",            // 公共接口
                        "/uploads/**",               // 文件上传路径
                        "/error",                    // 错误页面
                        "/favicon.ico",              // 网站图标
                        "/swagger-ui/**",            // Swagger UI
                        "/swagger-resources/**",     // Swagger资源
                        "/v2/api-docs",              // API文档
                        "/webjars/**"                // WebJars资源
                );
    }
}
