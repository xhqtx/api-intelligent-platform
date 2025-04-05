package com.example.auth.controller;

import com.example.auth.controller.dto.*;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.common.security.ApiResponse;
import com.example.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.auth.utils.PageUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('user:view', 'user:manage', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDirection", required = false) String sortDirection) {
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        Page<User> users = userService.findAllUsers(pageable);
        Page<UserDTO> userDTOs = users.map(UserDTO::new);
        return ResponseEntity.ok(ApiResponse.success(userDTOs));
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(ApiResponse.success(new UserDTO(user)));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getPassword(), request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(new UserDTO(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String username, @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.updateUser(username, request.getEmail(), request.isEnabled());
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{username}/password")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<ApiResponse<?>> changePassword(@PathVariable String username, @RequestParam String newPassword) {
        try {
            userService.changePassword(username, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{username}/roles")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<Set<Role>>> getUserRoles(@PathVariable String username) {
        try {
            Set<Role> roles = userService.getUserRoles(username);
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/role")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]')")
    public ResponseEntity<ApiResponse<?>> addRoleToUser(@RequestBody UserRoleRequest request) {
        try {
            userService.addRoleToUser(request.getUsername(), request.getRoleName());
            return ResponseEntity.ok(ApiResponse.success("Role added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/role")
    @PreAuthorize("hasAnyAuthority('ADMIN', '[ADMIN]')")
    public ResponseEntity<ApiResponse<?>> removeRoleFromUser(@RequestBody UserRoleRequest request) {
        try {
            userService.removeRoleFromUser(request.getUsername(), request.getRoleName());
            return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}