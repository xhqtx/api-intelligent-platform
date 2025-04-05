package com.example.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class UserUpdateRequest {

    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Enabled status cannot be null")
    private boolean enabled;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String email, boolean enabled) {
        this.email = email;
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}