package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Transaction {
    // Firestore ID là String
    private String transactionId;

    private String userId;

    private String walletId;

    private String categoryId;

    private Double amount; // Dùng Double tương thích tốt nhất với Firestore

    private String type;

    private String transactionDate; // Lưu String (ISO 8601)

    private String description;
}