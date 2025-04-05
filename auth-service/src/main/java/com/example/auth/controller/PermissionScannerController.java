package com.example.auth.controller;

import com.example.auth.model.Permission;
import com.example.auth.model.Role;
import com.example.auth.service.PermissionService;
import com.example.auth.service.RoleService;
import com.example.common.security.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class PermissionScannerController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @PostMapping("/scan-permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Set<String>> scanAndUpdatePermissions() {
        Set<String> newPermissions = new HashSet<>();
        Set<String> allPermissions = new HashSet<>();

        // 获取所有的RequestMapping
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        // 遍历所有的HandlerMethod，提取PreAuthorize注解中的权限
        for (HandlerMethod handlerMethod : handlerMethods.values()) {
            PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (preAuthorize != null) {
                String[] authorities = extractAuthorities(preAuthorize.value());
                for (String authority : authorities) {
                    if (authority != null && !authority.trim().isEmpty()) {
                        allPermissions.add(authority);
                        try {
                            Permission existingPermission = permissionService.findByName(authority);
                            if (existingPermission == null) {
                                Permission newPermission = permissionService.createPermission(authority, "Auto-generated permission");
                                newPermissions.add(newPermission.getName());
                            }
                        } catch (Exception e) {
                            // 记录错误但继续处理其他权限
                            System.err.println("Error processing permission '" + authority + "': " + e.getMessage());
                        }
                    }
                }
            }
        }

        // 为ADMIN角色添加所有权限
        Role adminRole = roleService.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        for (String permissionName : allPermissions) {
            Permission permission = permissionService.findByName(permissionName);
            if (permission != null && !adminRole.getPermissions().contains(permission)) {
                roleService.addPermissionToRole(adminRole.getId(), permission.getId());
            }
        }

        return ApiResponse.success("Permissions scanned and updated successfully", newPermissions);
    }

    private String[] extractAuthorities(String preAuthorizeExpression) {
        // 提取hasAuthority或hasAnyAuthority中的权限名
        Set<String> authorities = new HashSet<>();
        
        // 处理hasAuthority('ROLE_XXX')格式
        if (preAuthorizeExpression.contains("hasAuthority")) {
            int start = preAuthorizeExpression.indexOf("'");
            int end = preAuthorizeExpression.lastIndexOf("'");
            if (start != -1 && end != -1 && start < end) {
                String authority = preAuthorizeExpression.substring(start + 1, end);
                authorities.add(authority);
            }
        }
        
        // 处理hasAnyAuthority('ROLE_X', 'ROLE_Y')格式
        if (preAuthorizeExpression.contains("hasAnyAuthority")) {
            String[] parts = preAuthorizeExpression.split("'");
            for (int i = 1; i < parts.length; i += 2) {
                if (i < parts.length) {
                    String authority = parts[i].trim();
                    if (!authority.isEmpty()) {
                        authorities.add(authority);
                    }
                }
            }
        }
        
        return authorities.toArray(new String[0]);
    }
}