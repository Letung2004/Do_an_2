package com.example.personalfinancebackend.report.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryReportDTO {
    private String categoryId; // Thêm ID nếu cần
    private String categoryName;
    private BigDecimal totalAmount;
    private double percentage;

    public CategoryReportDTO(String categoryName, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }
}