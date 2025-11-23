package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Category {

    private String categoryId; // Firestore String ID

    private String userId; // userId có thể null nếu là danh mục mặc định

    private String name;

    private String type; // Lưu "income" hoặc "expense" dạng String cho đơn giản

    // Enum giữ nguyên để dùng cho logic khác nếu cần, nhưng lưu vào DB thì dùng String
    public enum TransactionType {
        income,
        expense
    }
}