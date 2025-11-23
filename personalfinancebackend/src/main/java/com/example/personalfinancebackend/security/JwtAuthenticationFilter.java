package com.example.personalfinancebackend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 1. Xác thực Token với Firebase
                // Hàm này sẽ ném lỗi nếu token giả, hết hạn, hoặc không đúng định dạng
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(jwt);

                // 2. Lấy thông tin User từ Token đã xác thực
                String uid = decodedToken.getUid(); // Firebase UID (Chuỗi String, không phải Long)
                String email = decodedToken.getEmail();

                // 3. Tạo Principal tùy chỉnh để dùng trong Controller
                // Lưu ý: ID bây giờ là String (UID), không phải Long
                CustomUserPrincipal principal = new CustomUserPrincipal(uid, email);

                // 4. Set Authentication cho Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (FirebaseAuthException e) {
            logger.error("Lỗi xác thực Firebase Token: " + e.getMessage());
            // Bạn có thể response lỗi 401 ở đây nếu muốn chặn ngay lập tức
        } catch (Exception ex) {
            logger.error("Không thể set user authentication", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Class Principal dùng để truyền dữ liệu vào Controller
    // @AuthenticationPrincipal CustomUserPrincipal principal
    public static class CustomUserPrincipal {
        private final String uid; // Đổi từ Long id sang String uid
        private final String email;

        public CustomUserPrincipal(String uid, String email) {
            this.uid = uid;
            this.email = email;
        }

        public String getId() { // Getter này trả về UID String
            return uid;
        }

        public String getEmail() {
            return email;
        }
    }
}