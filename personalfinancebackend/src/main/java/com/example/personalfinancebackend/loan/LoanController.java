package com.example.personalfinancebackend.loan;

import com.example.personalfinancebackend.loan.dto.LoanPaymentDTO;
import com.example.personalfinancebackend.loan.dto.LoanRequestDTO;
import com.example.personalfinancebackend.loan.dto.LoanResponseDTO;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * API: Ghi nhận Khoản Vay/Nợ mới
     */
    @PostMapping
    public ResponseEntity<?> createLoan(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody LoanRequestDTO requestDTO) {

        try {
            LoanResponseDTO newLoan = loanService.createLoan(requestDTO, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newLoan);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }

    /**
     * API: Xem Danh sách Vay/Nợ
     */
    @GetMapping
    public ResponseEntity<?> getMyLoans(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {

        try {
            List<LoanResponseDTO> loans = loanService.getLoans(principal.getId());
            return ResponseEntity.ok(loans);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }

    /**
     * API: Ghi nhận Trả nợ / Thu nợ
     */
    @PostMapping("/{loanId}/payment")
    public ResponseEntity<?> recordLoanPayment(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String loanId,
            @Valid @RequestBody LoanPaymentDTO paymentDTO) {

        try {
            LoanResponseDTO updatedLoan = loanService.recordPayment(
                    loanId, paymentDTO, principal.getId());
            return ResponseEntity.ok(updatedLoan);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Sửa (Cập nhật) thông tin Khoản Vay/Nợ
     */
    @PutMapping("/{loanId}")
    public ResponseEntity<?> updateLoan(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String loanId,
            @Valid @RequestBody LoanRequestDTO requestDTO) {

        try {
            LoanResponseDTO updatedLoan = loanService.updateLoan(
                    loanId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedLoan);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Đánh dấu là Đã trả hết (Thao tác thủ công)
     */
    @PostMapping("/{loanId}/mark-as-paid")
    public ResponseEntity<?> markLoanAsPaid(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String loanId) {

        try {
            LoanResponseDTO updatedLoan = loanService.markAsPaid(loanId, principal.getId());
            return ResponseEntity.ok(updatedLoan);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}