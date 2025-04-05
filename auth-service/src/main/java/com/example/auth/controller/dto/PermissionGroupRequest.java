package com.example.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PermissionGroupRequest {
    @NotBlank(message = "Permission group name cannot be empty")
    @Size(min = 2, max = 50, message = "Permission group name must be between 2 and 50 characters")
    private String name;

    @Size(max = 255, message = "Permission group description cannot exceed 255 characters")
    private String description;

    private List<Long> permissionIds;
}