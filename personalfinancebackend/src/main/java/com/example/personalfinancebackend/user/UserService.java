package com.example.personalfinancebackend.user;

import com.example.personalfinancebackend.model.User;
import com.example.personalfinancebackend.user.dto.UserResponseDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";

    /**
     * Lấy thông tin người dùng bằng ID (UID)
     */
    public UserResponseDTO getUserInfoById(String userId) throws ExecutionException, InterruptedException {
        // Lấy document tại path: users/{userId}
        ApiFuture<DocumentSnapshot> future = db.collection(COL_USERS).document(userId).get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            User user = document.toObject(User.class);
            if (user != null) {
                user.setUserId(document.getId());
                return UserResponseDTO.fromModel(user);
            }
        }

        // Trường hợp user đã đăng nhập (có token) nhưng chưa có data trong Firestore
        // Ta có thể trả về DTO rỗng hoặc bắn lỗi.
        // Ở đây mình trả về lỗi để dễ debug
        throw new RuntimeException("Không tìm thấy thông tin người dùng trong CSDL");
    }
}