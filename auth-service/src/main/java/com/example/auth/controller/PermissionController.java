package com.example.auth.controller;

import com.example.auth.controller.dto.PermissionRequest;
import com.example.auth.controller.dto.PermissionResponse;
import com.example.auth.model.Permission;
import com.example.auth.service.PermissionService;
import com.example.auth.utils.PageUtils;
import com.example.common.security.ApiResponse;
import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        Permission permission = permissionService.createPermission(request.getName(), request.getDescription());
        return ApiResponse.success("权限创建成功", toPermissionResponse(permission));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public ApiResponse<PermissionResponse> getPermission(@PathVariable Long id) {
        Optional<Permission> permissionOptional = permissionService.getPermissionById(id);
        return permissionOptional.map(permission -> 
            ApiResponse.success("获取权限成功", toPermissionResponse(permission)))
            .orElseGet(() -> ApiResponse.<PermissionResponse>error("权限不存在"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public ApiResponse<Page<PermissionResponse>> listPermissions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        // 已经使用了 PageUtils.convertToPageable，无需修改
        Page<Permission> permissions = permissionService.listPermissions(pageable);
        Page<PermissionResponse> responsePage = permissions.map(this::toPermissionResponse);
        return ApiResponse.success("获取权限列表成功", responsePage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public ApiResponse<PermissionResponse> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        Optional<Permission> existingPermission = permissionService.getPermissionById(id);
        if (existingPermission.isPresent()) {
            Permission permission = existingPermission.get();
            if (!permission.getName().equals(request.getName())) {
                return ApiResponse.<PermissionResponse>error("权限名称不允许修改");
            }
            Optional<Permission> updatedPermission = permissionService.updatePermission(id, permission.getName(), request.getDescription());
            return updatedPermission.map(p -> 
                ApiResponse.success("权限更新成功", toPermissionResponse(p)))
                .orElseGet(() -> ApiResponse.<PermissionResponse>error("权限更新失败"));
        }
        return ApiResponse.<PermissionResponse>error("权限不存在");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        boolean deleted = permissionService.deletePermission(id);
        return deleted ? ApiResponse.<Void>success("权限删除成功", null) : ApiResponse.<Void>error("权限删除失败，可能权限不存在");
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setDescription(permission.getDescription());
        response.setCreatedBy(permission.getCreatedBy());
        response.setUpdatedBy(permission.getUpdatedBy());
        
        // 添加空值检查，避免NullPointerException
        if (permission.getCreatedAt() != null) {
            response.setCreatedAt(permission.getCreatedAt().toString());
        }
        
        if (permission.getUpdatedAt() != null) {
            response.setUpdatedAt(permission.getUpdatedAt().toString());
        } else {
            // 如果更新时间为空，可以设置为创建时间或空字符串
            response.setUpdatedAt(permission.getCreatedAt() != null ? permission.getCreatedAt().toString() : "");
        }
        
        return response;
    }
}