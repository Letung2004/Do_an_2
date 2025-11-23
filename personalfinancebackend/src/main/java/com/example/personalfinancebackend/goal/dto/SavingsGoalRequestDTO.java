package com.example.personalfinancebackend.goal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingsGoalRequestDTO {

    private String categoryId; // Sửa Long -> String (Có thể null)

    @NotBlank(message = "Tên mục tiêu không được để trống")
    @Size(max = 150, message = "Tên mục tiêu không được quá 150 ký tự")
    private String name;

    @NotNull(message = "Số tiền mục tiêu không được để trống")
    @Positive(message = "Số tiền mục tiêu phải lớn hơn 0")
    private BigDecimal targetAmount;

    @NotNull(message = "Ngày mục tiêu không được để trống")
    // @FutureOrPresent // Có thể tạm bỏ nếu muốn test ngày cũ
    private LocalDate targetDate;
}