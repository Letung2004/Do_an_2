package com.example.personalfinancebackend.service;

import com.example.personalfinancebackend.model.Category;
import com.example.personalfinancebackend.model.dto.CategoryDTO;
import com.example.personalfinancebackend.model.dto.CategoryRequestDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CategoryService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_CATEGORIES = "categories"; // Danh mục Custom nằm trong Users, hoặc gốc?
    // CHÚ Ý: Để đơn giản cho việc lấy danh mục mặc định, ta nên để cấu trúc:
    // 1. Mặc định: Collection gốc "categories" (userId = null)
    // 2. Custom: Collection con "users/{uid}/categories" (userId = uid)
    // Hoặc: Tất cả trong collection gốc "categories" và lọc theo userId.

    // -> CHỌN CÁCH: Tất cả trong collection con "users/{uid}/categories" cho custom.
    // Và một collection riêng "default_categories" cho mặc định.

    /**
     * API: Lấy danh sách (Của User + Mặc định)
     */
    public List<CategoryDTO> getCategoriesForUser(String userId) throws ExecutionException, InterruptedException {
        List<CategoryDTO> result = new ArrayList<>();

        // 1. Lấy danh mục Mặc định (Global)
        CollectionReference defaultRef = db.collection("default_categories");
        ApiFuture<QuerySnapshot> defaultQuery = defaultRef.get();
        for (DocumentSnapshot doc : defaultQuery.get().getDocuments()) {
            Category cat = doc.toObject(Category.class);
            cat.setCategoryId(doc.getId());
            result.add(CategoryDTO.fromModel(cat));
        }

        // 2. Lấy danh mục Custom (Của User)
        CollectionReference userCatRef = db.collection(COL_USERS).document(userId).collection("categories");
        ApiFuture<QuerySnapshot> userQuery = userCatRef.get();
        for (DocumentSnapshot doc : userQuery.get().getDocuments()) {
            Category cat = doc.toObject(Category.class);
            cat.setCategoryId(doc.getId());
            cat.setUserId(userId); // Đảm bảo set userId
            result.add(CategoryDTO.fromModel(cat));
        }

        return result;
    }

    /**
     * API: Tạo Danh mục mới (Custom)
     */
    public CategoryDTO createCategory(CategoryRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        // Lưu vào sub-collection: users/{userId}/categories
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection("categories").document();

        Category category = new Category();
        category.setCategoryId(docRef.getId());
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setType(dto.getType().toString()); // Enum -> String

        ApiFuture<WriteResult> result = docRef.set(category);
        result.get();

        return CategoryDTO.fromModel(category);
    }

    /**
     * API: Sửa Danh mục (Chỉ sửa Custom)
     */
    public CategoryDTO updateCategory(String categoryId, CategoryRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection("categories").document(categoryId);

        DocumentSnapshot snap = docRef.get().get();
        if (!snap.exists()) {
            throw new IllegalArgumentException("Không tìm thấy danh mục (hoặc bạn không có quyền sửa danh mục mặc định)");
        }

        docRef.update("name", dto.getName(), "type", dto.getType().toString()).get();

        Category updatedCat = snap.toObject(Category.class);
        updatedCat.setCategoryId(categoryId);
        updatedCat.setName(dto.getName());
        updatedCat.setType(dto.getType().toString());

        return CategoryDTO.fromModel(updatedCat);
    }

    /**
     * API: Xóa Danh mục
     */
    public void deleteCategory(String categoryId, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection("categories").document(categoryId);

        // Kiểm tra tồn tại
        if (!docRef.get().get().exists()) {
            throw new IllegalArgumentException("Không tìm thấy danh mục tùy chỉnh này.");
        }

        // TODO: Kiểm tra ràng buộc (Constraint) với Transaction
        // Query xem có transaction nào dùng categoryId này không
        Query txQuery = db.collection(COL_USERS).document(userId).collection("transactions")
                .whereEqualTo("categoryId", categoryId).limit(1);

        if (!txQuery.get().get().isEmpty()) {
            throw new IllegalArgumentException("Không thể xóa: Danh mục đang được sử dụng.");
        }

        // Xóa
        docRef.delete().get();
    }
}