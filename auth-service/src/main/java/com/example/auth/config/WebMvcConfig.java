package com.example.auth.config;

import com.example.auth.interceptor.AuditLogInterceptor;
import com.example.auth.service.AuditLogService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditLogService auditLogService;

    public WebMvcConfig(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditLogInterceptor(auditLogService))
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");
    }
}