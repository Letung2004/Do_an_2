package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Budget {

    private String budgetId; // Firestore String ID

    private String userId;

    private String categoryId; // Chỉ lưu ID

    private String categoryName; // Lưu luôn tên để đỡ phải query lại Category

    private Double amountLimit;

    private Double currentSpent; // TRƯỜNG MỚI: Tổng tiền đã chi (được update realtime)

    private String startDate; // Lưu String ISO (YYYY-MM-DD)

    private String endDate;   // Lưu String ISO (YYYY-MM-DD)
}