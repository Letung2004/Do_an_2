package com.example.personalfinancebackend.loan.dto;

import com.example.personalfinancebackend.model.Loan;
import com.example.personalfinancebackend.model.enums.LoanStatus;
import com.example.personalfinancebackend.model.enums.LoanType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanResponseDTO {
    private String loanId; // Sửa Long -> String
    private LoanType type;
    private String personName;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private String dueDate; // Trả về String ISO
    private LoanStatus status;

    public static LoanResponseDTO fromModel(Loan loan) {
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setLoanId(loan.getLoanId());
        dto.setType(loan.getType());
        dto.setPersonName(loan.getPersonName());

        // Convert Double (Firestore) -> BigDecimal (API)
        dto.setAmount(BigDecimal.valueOf(loan.getAmount() != null ? loan.getAmount() : 0.0));
        dto.setRemainingAmount(BigDecimal.valueOf(loan.getRemainingAmount() != null ? loan.getRemainingAmount() : 0.0));

        dto.setDueDate(loan.getDueDate());
        dto.setStatus(loan.getStatus());
        return dto;
    }
}