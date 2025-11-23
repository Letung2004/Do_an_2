package com.example.personalfinancebackend.model.dto;

import com.example.personalfinancebackend.model.Category;
import jakarta.validation.constraints.*; // <-- Import
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được quá 100 ký tự")
    private String name;

    @NotNull(message = "Loại danh mục không được để trống")
    private Category.TransactionType type;
}