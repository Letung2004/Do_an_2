package com.example.personalfinancebackend.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletListResponseDTO {

    private List<WalletDTO> wallets;
    private BigDecimal totalBalance;

    public WalletListResponseDTO(List<WalletDTO> wallets) {
        this.wallets = wallets;
        this.totalBalance = wallets.stream()
                .map(WalletDTO::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}