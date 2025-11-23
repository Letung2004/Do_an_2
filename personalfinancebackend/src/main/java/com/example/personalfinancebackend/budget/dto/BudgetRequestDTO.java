package com.example.personalfinancebackend.budget.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetRequestDTO {

    @NotNull(message = "ID Danh mục không được để trống")
    private String categoryId; // Sửa Long -> String

    @NotNull(message = "Giới hạn tiền không được để trống")
    @Positive(message = "Giới hạn tiền phải lớn hơn 0")
    private BigDecimal amountLimit;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
}