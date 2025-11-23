package com.example.personalfinancebackend.controller;

import com.example.personalfinancebackend.model.dto.CategoryDTO;
import com.example.personalfinancebackend.model.dto.CategoryRequestDTO;
import com.example.personalfinancebackend.security.JwtAuthenticationFilter;
import com.example.personalfinancebackend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getMyCategories(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal) {
        try {
            List<CategoryDTO> categories = categoryService.getCategoriesForUser(principal.getId());
            return ResponseEntity.ok(categories);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        try {
            CategoryDTO newCategory = categoryService.createCategory(categoryRequestDTO, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String categoryId, // Sửa Long -> String
            @Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryRequestDTO, principal.getId());
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @AuthenticationPrincipal JwtAuthenticationFilter.CustomUserPrincipal principal,
            @PathVariable String categoryId) { // Sửa Long -> String
        try {
            categoryService.deleteCategory(categoryId, principal.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}