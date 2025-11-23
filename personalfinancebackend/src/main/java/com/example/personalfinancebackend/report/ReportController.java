package com.example.personalfinancebackend.report;

import com.example.personalfinancebackend.report.dto.CategoryReportDTO;
import com.example.personalfinancebackend.report.dto.DailySummaryReportDTO;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/expense-by-category")
    public ResponseEntity<?> getExpenseByCategoryReport(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<CategoryReportDTO> report = reportService.getExpenseReport(
                    principal.getId(), startDate, endDate); // ID là String
            return ResponseEntity.ok(report);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<?> getDailySummaryReport(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<DailySummaryReportDTO> report = reportService.getDailySummaryReport(
                    principal.getId(), startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}