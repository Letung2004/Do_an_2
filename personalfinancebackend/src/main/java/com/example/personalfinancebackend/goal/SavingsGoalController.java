package com.example.personalfinancebackend.goal;

import com.example.personalfinancebackend.goal.dto.*;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/savings-goals")
public class SavingsGoalController {

    @Autowired
    private SavingsGoalService savingsGoalService;

    /**
     * API: TẠO MỚI một mục tiêu tiết kiệm
     */
    @PostMapping
    public ResponseEntity<?> createSavingsGoal(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody SavingsGoalRequestDTO requestDTO) {

        try {
            SavingsGoalResponseDTO newGoal = savingsGoalService.createGoal(requestDTO, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newGoal);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException là đủ
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: SỬA (Cập nhật) một mục tiêu tiết kiệm
     */
    @PutMapping("/{goalId}")
    public ResponseEntity<?> updateSavingsGoal(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String goalId,
            @Valid @RequestBody SavingsGoalRequestDTO requestDTO) {

        try {
            SavingsGoalResponseDTO updatedGoal = savingsGoalService.updateGoal(
                    goalId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: XÓA một mục tiêu tiết kiệm
     */
    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteSavingsGoal(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String goalId) {

        try {
            savingsGoalService.deleteGoal(goalId, principal.getId());
            return ResponseEntity.noContent().build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: XEM DANH SÁCH tất cả mục tiêu
     */
    @GetMapping
    public ResponseEntity<?> getMySavingsGoals(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {

        try {
            List<SavingsGoalResponseDTO> goals = savingsGoalService.getGoals(principal.getId());
            return ResponseEntity.ok(goals);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }

    /**
     * API: NẠP TIỀN vào một mục tiêu tiết kiệm
     */
    @PostMapping("/{goalId}/add-money")
    public ResponseEntity<?> addMoneyToGoal(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String goalId,
            @Valid @RequestBody AddMoneyToGoalRequestDTO requestDTO) {

        try {
            SavingsGoalResponseDTO updatedGoal = savingsGoalService.addMoneyToGoal(
                    goalId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: RÚT TIỀN khỏi một mục tiêu tiết kiệm
     */
    @PostMapping("/{goalId}/withdraw-money")
    public ResponseEntity<?> withdrawMoneyFromGoal(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String goalId,
            @Valid @RequestBody WithdrawFromGoalRequestDTO requestDTO) {

        try {
            SavingsGoalResponseDTO updatedGoal = savingsGoalService.withdrawMoneyFromGoal(
                    goalId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        } catch (RuntimeException e) { // SỬA: Chỉ cần bắt RuntimeException
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}