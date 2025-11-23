package com.example.personalfinancebackend.budget;

import com.example.personalfinancebackend.budget.dto.BudgetRequestDTO;
import com.example.personalfinancebackend.budget.dto.BudgetResponseDTO;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<?> createBudget(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody BudgetRequestDTO requestDTO) {
        try {
            BudgetResponseDTO newBudget = budgetService.createBudget(requestDTO, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newBudget);
        } catch (ExecutionException | InterruptedException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyBudgets(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {
        try {
            List<BudgetResponseDTO> budgets = budgetService.getBudgets(principal.getId());
            return ResponseEntity.ok(budgets);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<?> updateBudget(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String budgetId, // Sửa Long -> String
            @Valid @RequestBody BudgetRequestDTO requestDTO) {
        try {
            BudgetResponseDTO updatedBudget = budgetService.updateBudget(budgetId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedBudget);
        } catch (ExecutionException | InterruptedException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<?> deleteBudget(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String budgetId) { // Sửa Long -> String

        budgetService.deleteBudget(budgetId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}