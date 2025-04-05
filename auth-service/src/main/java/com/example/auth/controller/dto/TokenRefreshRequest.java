package com.example.auth.controller.dto;

public class TokenRefreshRequest {
    private String refreshToken;

    // Getter and Setter
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}