package com.example.auth.controller.dto;

import lombok.Data;

@Data
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;
}