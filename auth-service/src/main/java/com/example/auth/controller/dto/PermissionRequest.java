package com.example.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {
    @NotBlank(message = "权限名称不能为空")
    private String name;
    
    private String description;
}