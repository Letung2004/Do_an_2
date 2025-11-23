package com.example.personalfinancebackend.user.dto;

import com.example.personalfinancebackend.model.User;
import lombok.Data;

@Data
public class UserResponseDTO {

    private String userId;
    private String email;
    private String fullName;

    public static UserResponseDTO fromModel(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}