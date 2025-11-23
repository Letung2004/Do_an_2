package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    private String userId;
    private String email;
    private String fullName;
    private String createdAt;

    // TRƯỜNG MỚI: Lưu token để gửi thông báo
    private String fcmToken;
}