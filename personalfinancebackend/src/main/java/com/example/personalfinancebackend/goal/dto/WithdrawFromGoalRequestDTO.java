package com.example.personalfinancebackend.goal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawFromGoalRequestDTO {

    @NotNull(message = "ID Ví đích không được để trống")
    private String walletId; // Sửa Long -> String

    @NotNull(message = "ID Danh mục thu không được để trống")
    private String categoryId; // Sửa Long -> String

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
}