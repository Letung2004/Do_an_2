package com.example.personalfinancebackend.model.dto;

import com.example.personalfinancebackend.model.Category;
import lombok.Data;

@Data
public class CategoryDTO {
    private String categoryId;
    private String name;
    private String type; // Trả về String
    private boolean isDefault;

    public static CategoryDTO fromModel(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        // Logic: Mặc định nếu userId là null hoặc rỗng
        dto.setDefault(category.getUserId() == null || category.getUserId().isEmpty());
        return dto;
    }
}