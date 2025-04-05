package com.example.auth.controller.dto;

import lombok.Data;

@Data
public class UserRoleRequest {
    private String username;
    private String roleName;
}