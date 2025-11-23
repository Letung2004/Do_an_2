package com.example.personalfinancebackend.loan.dto;

import com.example.personalfinancebackend.model.enums.LoanType;
import jakarta.validation.constraints.*; // <-- Import
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRequestDTO {

    @NotNull(message = "Loại Vay/Nợ không được để trống")
    private LoanType type;

    @NotBlank(message = "Tên người liên quan không được để trống")
    @Size(max = 100, message = "Tên không được quá 100 ký tự")
    private String personName;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Ngày đáo hạn không được để trống")
    private LocalDate dueDate;
}