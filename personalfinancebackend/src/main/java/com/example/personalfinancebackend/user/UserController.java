package com.example.personalfinancebackend.user;

import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import com.example.personalfinancebackend.user.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * API: Lấy thông tin của người dùng đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {

        // Lấy UID String từ token
        String userId = principal.getId();

        try {
            UserResponseDTO userInfo = userService.getUserInfoById(userId);
            return ResponseEntity.ok(userInfo);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}