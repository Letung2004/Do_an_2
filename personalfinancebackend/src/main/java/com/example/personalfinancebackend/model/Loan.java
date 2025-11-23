package com.example.personalfinancebackend.model;

import com.example.personalfinancebackend.model.enums.LoanStatus;
import com.example.personalfinancebackend.model.enums.LoanType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Loan {

    private String loanId; // Firestore String ID

    private String userId;

    private LoanType type; // Enum (Firestore sẽ lưu String)

    private String personName;

    private Double amount; // Số tiền gốc

    private Double remainingAmount; // Số tiền còn lại

    private String dueDate; // String ISO (YYYY-MM-DD)

    private LoanStatus status = LoanStatus.unpaid;
}