package com.example.personalfinancebackend.controller;

import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import com.example.personalfinancebackend.service.TransactionService;
import com.example.personalfinancebackend.transaction.TransactionRequestDTO;
import com.example.personalfinancebackend.transaction.TransactionResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // --- 1. TẠO GIAO DỊCH MỚI ---
    @PostMapping
    public ResponseEntity<?> createTransaction(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody TransactionRequestDTO transactionDTO) {

        // SỬA: Lấy ID là String (UID từ Firebase)
        String currentUserId = principal.getId();

        try {
            TransactionResponseDTO responseDTO = transactionService.addTransaction(transactionDTO, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Firestore: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 2. LẤY DANH SÁCH GIAO DỊCH (LỌC) ---
    @GetMapping
    public ResponseEntity<?> getTransactions(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,

            // SỬA: Các ID tham chiếu đổi sang String
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String categoryId
            // Tạm bỏ các filter ngày tháng để test cơ bản trước
    ) {
        String currentUserId = principal.getId();

        try {
            // Gọi Service (Hàm này trả về List Map như code Service trước đó)
            List<Map<String, Object>> transactions = transactionService.getTransactions(currentUserId, walletId, categoryId);
            return ResponseEntity.ok(transactions);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }
    }

    // --- 3. XÓA GIAO DỊCH ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable String id, // SỬA: ID giao dịch là String
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {

        String currentUserId = principal.getId();

        // Lưu ý: Service cần cập nhật hàm delete để xử lý trả lại tiền vào ví (nếu muốn logic chặt chẽ)
        transactionService.deleteTransaction(currentUserId, id);

        return ResponseEntity.noContent().build();
    }
}