package com.example.personalfinancebackend.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanPaymentDTO {

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "ID Ví không được để trống")
    private String walletId; // Sửa Long -> String

    @NotNull(message = "ID Danh mục không được để trống")
    private String categoryId; // Sửa Long -> String
}