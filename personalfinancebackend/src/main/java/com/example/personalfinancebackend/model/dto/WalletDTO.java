package com.example.personalfinancebackend.model.dto;

import com.example.personalfinancebackend.model.Wallet;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletDTO {
    private String walletId; // Sửa Long -> String
    private String name;
    private BigDecimal balance;

    public static WalletDTO fromModel(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setWalletId(wallet.getWalletId());
        dto.setName(wallet.getName());
        // Chuyển Double về BigDecimal để trả về JSON chuẩn tiền tệ
        dto.setBalance(BigDecimal.valueOf(wallet.getBalance() != null ? wallet.getBalance() : 0.0));
        return dto;
    }
}