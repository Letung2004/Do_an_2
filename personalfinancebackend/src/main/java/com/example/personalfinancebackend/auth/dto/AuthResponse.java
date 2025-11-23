package com.example.personalfinancebackend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; // Trả lại token (hoặc token mới nếu cần)
    private String userId;      // Sửa Long -> String
    private String email;
    private String fullName;
}