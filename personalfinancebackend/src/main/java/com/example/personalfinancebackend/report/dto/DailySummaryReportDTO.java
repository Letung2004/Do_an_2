package com.example.personalfinancebackend.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor // Constructor cho JPA @Query
public class DailySummaryReportDTO {
    private LocalDate date;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}