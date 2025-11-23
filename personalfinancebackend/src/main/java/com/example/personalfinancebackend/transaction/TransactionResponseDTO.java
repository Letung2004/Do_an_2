package com.example.personalfinancebackend.transaction;

import com.example.personalfinancebackend.model.Transaction;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private String transactionId; // Sửa Long -> String
    private String walletId;      // Sửa Long -> String
    private String walletName;
    private String categoryId;    // Sửa Long -> String
    private String categoryName;
    private Double amount;        // Firestore trả về Double
    private String type;
    private String transactionDate; // Trả về String ISO cho Flutter dễ parse
    private String description;

    // Hàm tiện ích chuyển từ Model Firestore sang DTO
    public static TransactionResponseDTO fromModel(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());

        dto.setWalletId(transaction.getWalletId());
        dto.setCategoryId(transaction.getCategoryId());

        // Lưu ý: Tên ví/hạng mục nếu không lưu trong Transaction
        // thì sẽ null, phải query riêng nếu cần hiển thị.
        return dto;
    }
}