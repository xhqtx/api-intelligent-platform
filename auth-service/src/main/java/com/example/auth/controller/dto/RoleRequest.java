package com.example.auth.controller.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RoleRequest {
    private String name;
    private String description;
    private Boolean status;
    private Set<Long> permissions;
    private Set<Long> users;
}