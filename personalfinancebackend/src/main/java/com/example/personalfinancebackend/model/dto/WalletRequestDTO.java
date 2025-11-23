package com.example.personalfinancebackend.model.dto;

import jakarta.validation.constraints.*; // <-- Import
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletRequestDTO {

    @NotBlank(message = "Tên ví không được để trống") // @NotBlank = @NotNull + không rỗng
    @Size(min = 1, max = 100, message = "Tên ví phải từ 1 đến 100 ký tự")
    private String name;

    @NotNull(message = "Số dư ban đầu không được null")
    @PositiveOrZero(message = "Số dư ban đầu phải lớn hơn hoặc bằng 0")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}