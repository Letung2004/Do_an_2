package com.example.personalfinancebackend.budget.dto;

import com.example.personalfinancebackend.model.Budget;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
public class BudgetResponseDTO {
    private String budgetId;
    private String categoryId;
    private String categoryName;
    private BigDecimal amountLimit;
    private String startDate;
    private String endDate;

    // Các trường tính toán
    private BigDecimal currentSpending;
    private BigDecimal remainingAmount;
    private double percentage;

    public static BudgetResponseDTO fromModel(Budget budget) {
        BudgetResponseDTO dto = new BudgetResponseDTO();
        dto.setBudgetId(budget.getBudgetId());
        dto.setCategoryId(budget.getCategoryId());
        dto.setCategoryName(budget.getCategoryName()); // Lấy từ Model luôn
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());

        BigDecimal limit = BigDecimal.valueOf(budget.getAmountLimit() != null ? budget.getAmountLimit() : 0.0);
        BigDecimal spent = BigDecimal.valueOf(budget.getCurrentSpent() != null ? budget.getCurrentSpent() : 0.0);

        dto.setAmountLimit(limit);
        dto.setCurrentSpending(spent);
        dto.setRemainingAmount(limit.subtract(spent));

        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            dto.setPercentage(spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue());
        } else {
            dto.setPercentage(0.0);
        }
        return dto;
    }
}