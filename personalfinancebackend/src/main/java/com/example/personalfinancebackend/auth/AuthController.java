package com.example.personalfinancebackend.auth;

import com.example.personalfinancebackend.auth.dto.AuthResponse;
import com.example.personalfinancebackend.auth.dto.GoogleLoginRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * API: Login với Google (Firebase Token)
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest loginRequest) {
        try {
            // GỌI HÀM VỚI 2 THAM SỐ: idToken và fcmToken
            // Biến authResponse được khai báo và trả về ngay trong khối try
            AuthResponse authResponse = authService.processGoogleLogin(
                    loginRequest.getIdToken(),
                    loginRequest.getFcmToken()
            );

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            // Nếu có lỗi, trả về 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Xác thực thất bại: " + e.getMessage());
        }
    }
}