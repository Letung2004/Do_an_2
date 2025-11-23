package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SavingsGoal {

    private String goalId; // Firestore ID

    private String userId;

    private String categoryId; // Có thể null

    private String name;

    private Double targetAmount;

    private Double currentAmount; // Mặc định 0.0

    private String targetDate; // String ISO
}