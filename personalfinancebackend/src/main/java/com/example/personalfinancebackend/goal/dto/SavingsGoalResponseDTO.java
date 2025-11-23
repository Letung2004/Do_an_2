package com.example.personalfinancebackend.goal.dto;

import com.example.personalfinancebackend.model.SavingsGoal;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingsGoalResponseDTO {
    private String goalId; // Sửa Long -> String
    private String categoryId; // Sửa Long -> String
    private String categoryName;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String targetDate; // Trả về String cho đồng bộ

    public static SavingsGoalResponseDTO fromModel(SavingsGoal goal) {
        SavingsGoalResponseDTO dto = new SavingsGoalResponseDTO();
        dto.setGoalId(goal.getGoalId());
        dto.setName(goal.getName());

        // Convert Double (Firestore) -> BigDecimal (API Response)
        dto.setTargetAmount(BigDecimal.valueOf(goal.getTargetAmount() != null ? goal.getTargetAmount() : 0.0));
        dto.setCurrentAmount(BigDecimal.valueOf(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : 0.0));

        dto.setTargetDate(goal.getTargetDate());
        dto.setCategoryId(goal.getCategoryId());

        // Lưu ý: categoryName cần query riêng hoặc lưu trong model nếu muốn hiện
        return dto;
    }
}