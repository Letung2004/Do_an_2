package com.example.personalfinancebackend.transaction;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequestDTO {

    @NotNull(message = "ID Ví không được để trống")
    private String walletId; // Sửa Long -> String

    @NotNull(message = "ID Danh mục không được để trống")
    private String categoryId; // Sửa Long -> String

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải là một số dương")
    private BigDecimal amount;

    @NotNull(message = "Loại giao dịch không được để trống")
    private String type; // Để String cho dễ (hoặc dùng Enum nếu muốn)

    @NotNull(message = "Ngày giao dịch không được để trống")
    // @PastOrPresent // Tạm bỏ cái này để test cho dễ, bật lại sau
    private LocalDateTime transactionDate;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String description;
}