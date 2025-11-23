package com.example.personalfinancebackend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    /**
     * Hàm gửi cảnh báo vượt ngân sách (Đã cập nhật 4 tham số)
     */
    public void sendBudgetAlert(String fcmToken, String categoryName, double percent, double amountLimit) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            System.out.println("User không có FCM Token, bỏ qua gửi thông báo.");
            return;
        }

        try {
            // 1. Nội dung thông báo
            String title = "⚠️ Cảnh báo Chi tiêu!";
            // Format tin nhắn có cả số phần trăm và hạn mức
            String body = String.format("Bạn đã tiêu %.0f%% ngân sách cho mục '%s' (Hạn mức: %.0f)",
                    percent, categoryName, amountLimit);

            // 2. Tạo Message
            Message message = Message.builder()
                    .setToken(fcmToken) // Gửi đến máy cụ thể
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    // (Tùy chọn) Data ẩn để App xử lý mở màn hình
                    .putData("screen", "budget_detail")
                    .putData("category", categoryName)
                    .build();

            // 3. Gửi ngay lập tức
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Đã gửi thông báo FCM thành công: " + response);

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi FCM: " + e.getMessage());
        }
    }
}