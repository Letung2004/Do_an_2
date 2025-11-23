package com.example.personalfinancebackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID Token không được để trống")
    private String idToken;

    // TRƯỜNG MỚI: Token của thiết bị (Flutter sẽ lấy và gửi lên)
    private String fcmToken;
}