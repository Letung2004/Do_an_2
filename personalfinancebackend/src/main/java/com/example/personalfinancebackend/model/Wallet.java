package com.example.personalfinancebackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Wallet {
    // Firestore ID là String
    private String walletId;

    private String userId; // Lưu ID String tham chiếu user

    private String name;

    private Double balance; // Firestore dùng Double
}