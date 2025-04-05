package com.example.auth.controller;

import com.example.auth.controller.dto.RoleDTO;
import com.example.auth.controller.dto.RoleRequest;
import com.example.auth.controller.dto.UserDTO;
import com.example.auth.controller.dto.PermissionDTO;
import com.example.auth.model.Role;
import com.example.auth.model.Permission;
import com.example.auth.model.User;
import com.example.auth.utils.PageUtils;
import com.example.common.security.ApiResponse;
import com.example.auth.service.RoleService;
import com.example.auth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(PermissionDTO::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<Set<PermissionDTO>>> getRolePermissions(@PathVariable Long roleId) {
        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<PermissionDTO> permissionDTOs = role.getPermissions().stream()
                .map(PermissionDTO::new)
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<?>> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission added to role successfully"));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<?>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<Page<RoleDTO>>> getRoles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        
        // 使用 PageUtils.convertToPageable 转换前端页码（从1开始）到后端页码（从0开始）
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        Page<Role> roles = roleService.findAll(name, status, pageable);

        Page<RoleDTO> roleDTOs = roles.map(RoleDTO::new);
        return ResponseEntity.ok(ApiResponse.success(roleDTOs));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<Role> roles = roleService.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(RoleDTO::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(roleDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(role -> ResponseEntity.ok(ApiResponse.success(new RoleDTO(role))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Role not found")));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@RequestBody RoleRequest roleRequest) {
        if (roleService.existsByName(roleRequest.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Role with this name already exists"));
        }
        Role role = roleService.createRole(roleRequest.getName(), roleRequest.getDescription(), roleRequest.getPermissions());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", new RoleDTO(role)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(@PathVariable Long id, @RequestBody RoleRequest roleRequest) {
        try {
            Role updatedRole = roleService.updateRole(id, roleRequest.getName(), roleRequest.getDescription(), roleRequest.getStatus(), roleRequest.getPermissions(), roleRequest.getUsers());
            return ResponseEntity.ok(ApiResponse.success("Role updated successfully", new RoleDTO(updatedRole)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Role not found"));
        }
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getRoleUsers(@PathVariable Long id) {
        List<User> users = roleService.findUsersByRoleId(id);
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(userDTOs));
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<?>> assignUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        roleService.assignUsersToRole(id, userIds);
        return ResponseEntity.ok(ApiResponse.success("Users assigned to role successfully"));
    }

    @DeleteMapping("/{id}/users")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<?>> removeUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        roleService.removeUsersFromRole(id, userIds);
        return ResponseEntity.ok(ApiResponse.success("Users removed from role successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<?>> deleteRole(@PathVariable Long id) {
        if (!roleService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Role not found"));
        }
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }
}