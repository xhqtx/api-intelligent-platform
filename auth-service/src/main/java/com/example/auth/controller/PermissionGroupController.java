package com.example.auth.controller;

import com.example.auth.controller.dto.PermissionGroupRequest;
import com.example.auth.model.PermissionGroup;
import com.example.auth.service.PermissionService;
import com.example.auth.utils.PageUtils;
import com.example.common.security.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/permission-groups")
public class PermissionGroupController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('permission:group:manage', 'role:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> createPermissionGroup(@Valid @RequestBody PermissionGroupRequest request) {
        PermissionGroup group = permissionService.createPermissionGroup(request.getName(), request.getDescription());
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            group = permissionService.addPermissionsToGroup(group.getId(), request.getPermissionIds());
        }
        return ResponseEntity.ok(ApiResponse.success("权限组创建成功", group));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> updatePermissionGroup(@PathVariable Long id, @Valid @RequestBody PermissionGroupRequest request) {
        PermissionGroup group = permissionService.updatePermissionGroup(id, request.getName(), request.getDescription());
        if (request.getPermissionIds() != null) {
            group = permissionService.addPermissionsToGroup(id, request.getPermissionIds());
        }
        return ResponseEntity.ok(ApiResponse.success("权限组更新成功", group));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<Void>> deletePermissionGroup(@PathVariable Long id) {
        permissionService.deletePermissionGroup(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("权限组删除成功", null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<Page<PermissionGroup>>> getAllPermissionGroups(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        Page<PermissionGroup> groups = permissionService.findAllPermissionGroups(name, pageable);

        return ResponseEntity.ok(ApiResponse.success("获取所有权限组成功", groups));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> getPermissionGroupById(@PathVariable Long id) {
        return permissionService.findPermissionGroupById(id)
                .map(group -> ResponseEntity.ok(ApiResponse.success("获取权限组成功", group)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<List<Long>>> getPermissionGroupPermissions(@PathVariable Long id) {
        List<Long> permissionIds = permissionService.getPermissionGroupPermissions(id);
        return ResponseEntity.ok(ApiResponse.success("获取权限组权限成功", permissionIds));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> updatePermissionGroupPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        PermissionGroup group = permissionService.updatePermissionGroupPermissions(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("更新权限组权限成功", group));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> addPermissionsToGroup(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        PermissionGroup group = permissionService.addPermissionsToGroup(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("权限添加到组成功", group));
    }

    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('permission:group:manage')")
    public ResponseEntity<ApiResponse<PermissionGroup>> removePermissionsFromGroup(@PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        PermissionGroup group = permissionService.removePermissionsFromGroup(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("权限从组中移除成功", group));
    }
}