package com.example.personalfinancebackend.controller;

import com.example.personalfinancebackend.model.dto.WalletDTO;
import com.example.personalfinancebackend.model.dto.WalletListResponseDTO;
import com.example.personalfinancebackend.model.dto.WalletRequestDTO;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import com.example.personalfinancebackend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<?> getMyWallets(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {
        try {
            WalletListResponseDTO response = walletService.getWalletsByUserId(principal.getId());
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createWallet(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        try {
            WalletDTO newWallet = walletService.createWallet(requestDTO, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<?> updateWallet(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String walletId, // Sửa Long -> String
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        try {
            WalletDTO updatedWallet = walletService.updateWallet(walletId, requestDTO, principal.getId());
            return ResponseEntity.ok(updatedWallet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<?> deleteWallet(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String walletId) { // Sửa Long -> String
        try {
            walletService.deleteWallet(walletId, principal.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}