package com.example.personalfinancebackend.auth;

import com.example.personalfinancebackend.auth.dto.AuthResponse;
import com.example.personalfinancebackend.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";

    /**
     * Xử lý đăng nhập Google (Firebase)
     * Token gửi lên là Firebase ID Token (lấy từ Flutter)
     */
    public AuthResponse processGoogleLogin(String idTokenString, String fcmToken) throws Exception {

        // 1. Xác thực Token
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idTokenString);
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();
        if (name == null) name = email;

        DocumentReference userRef = db.collection("users").document(uid);
        DocumentSnapshot userSnap = userRef.get().get();

        User user;

        if (userSnap.exists()) {
            // 2a. User cũ -> Cập nhật FCM Token mới nhất
            user = userSnap.toObject(User.class);
            if (user != null) user.setUserId(uid);

            // Cập nhật Token vào DB
            userRef.update("fcmToken", fcmToken);
        } else {
            // 2b. User mới -> Tạo kèm Token
            user = new User();
            user.setUserId(uid);
            user.setEmail(email);
            user.setFullName(name);
            user.setCreatedAt(LocalDateTime.now().toString());
            user.setFcmToken(fcmToken); // Lưu token

            userRef.set(user).get();
        }

        return new AuthResponse(idTokenString, uid, email, name);
    }
}