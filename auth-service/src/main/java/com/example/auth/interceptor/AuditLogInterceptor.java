package com.example.auth.interceptor;

import com.example.auth.model.AuditLog;
import com.example.auth.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;
import java.util.stream.Collectors;

public class AuditLogInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;

    public AuditLogInterceptor(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // Add a private method for logging audit events
    private void logAudit(String username, String action, String resourceType, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setDetails(details);
        auditLog.setIpAddress(ipAddress);
        auditLogService.saveAuditLog(auditLog);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Called before request processing, set request start time and other information here
        request.setAttribute("startTime", System.currentTimeMillis());
        
        // Wrap the request to enable content reading later
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        
        // Wrap the response to enable content reading later
        if (!(response instanceof ResponseWrapper)) {
            response = new ResponseWrapper(response);
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Called after request processing but before view rendering
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Called after the entire request is completed, log the audit information here
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(request.getMethod());
        auditLog.setResourceType(request.getRequestURI());
        auditLog.setIpAddress(request.getRemoteAddr());

        // Calculate request processing duration
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        // Get request content
        String requestContent = "";
        if (request instanceof ContentCachingRequestWrapper) {
            try {
                requestContent = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray());
            } catch (Exception e) {
                requestContent = "Failed to read request content: " + e.getMessage();
            }
        }

        // Get response content
        String responseContent = "";
        if (response instanceof ResponseWrapper) {
            try {
                responseContent = ((ResponseWrapper) response).getCaptureAsString();
            } catch (IOException e) {
                responseContent = "Failed to read response content: " + e.getMessage();
            }
        }

        // Set detailed information
        String details = String.format("Request processed in %d ms\nRequest Content: %s\nResponse Content: %s",
                duration, requestContent, responseContent);
        auditLog.setDetails(details);

        logAudit(username, auditLog.getAction(), auditLog.getResourceType(), details, auditLog.getIpAddress());
    }
}