package com.example.personalfinancebackend.budget;

import com.example.personalfinancebackend.budget.dto.BudgetRequestDTO;
import com.example.personalfinancebackend.budget.dto.BudgetResponseDTO;
import com.example.personalfinancebackend.model.Budget;
import com.example.personalfinancebackend.model.Category;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BudgetService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_BUDGETS = "budgets";
    private static final String COL_CATEGORIES = "categories";

    // --- 1. TẠO NGÂN SÁCH ---
    public BudgetResponseDTO createBudget(BudgetRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        // A. Lấy thông tin Category để lưu tên (denormalization)
        DocumentReference catRef = db.collection(COL_USERS).document(userId)
                .collection(COL_CATEGORIES).document(dto.getCategoryId());
        DocumentSnapshot catSnap = catRef.get().get();
        if (!catSnap.exists()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        String catName = catSnap.getString("name");

        // B. Tạo Budget mới
        DocumentReference docRef = db.collection(COL_USERS).document(userId)
                .collection(COL_BUDGETS).document();

        Budget budget = new Budget();
        budget.setBudgetId(docRef.getId());
        budget.setUserId(userId);
        budget.setCategoryId(dto.getCategoryId());
        budget.setCategoryName(catName); // Lưu tên vào luôn
        budget.setAmountLimit(dto.getAmountLimit().doubleValue());
        budget.setCurrentSpent(0.0); // Ban đầu là 0
        budget.setStartDate(dto.getStartDate().toString());
        budget.setEndDate(dto.getEndDate().toString());

        // C. Ghi vào DB
        docRef.set(budget).get();

        return BudgetResponseDTO.fromModel(budget);
    }

    // --- 2. LẤY DANH SÁCH ---
    public List<BudgetResponseDTO> getBudgets(String userId)
            throws ExecutionException, InterruptedException {

        CollectionReference budgetsRef = db.collection(COL_USERS).document(userId).collection(COL_BUDGETS);
        ApiFuture<QuerySnapshot> query = budgetsRef.get();

        List<BudgetResponseDTO> result = new ArrayList<>();
        for (DocumentSnapshot doc : query.get().getDocuments()) {
            Budget budget = doc.toObject(Budget.class);
            // Đảm bảo ID đúng
            budget.setBudgetId(doc.getId());
            result.add(BudgetResponseDTO.fromModel(budget));
        }
        return result;
    }

    // --- 3. SỬA NGÂN SÁCH ---
    public BudgetResponseDTO updateBudget(String budgetId, BudgetRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = db.collection(COL_USERS).document(userId)
                .collection(COL_BUDGETS).document(budgetId);

        // Kiểm tra tồn tại
        if (!docRef.get().get().exists()) {
            throw new IllegalArgumentException("Ngân sách không tồn tại");
        }

        // Cập nhật các trường
        // Lưu ý: Nếu đổi CategoryId thì phải lấy lại CategoryName, ở đây tạm bỏ qua cho đơn giản
        docRef.update(
                "amountLimit", dto.getAmountLimit().doubleValue(),
                "startDate", dto.getStartDate().toString(),
                "endDate", dto.getEndDate().toString()
        ).get();

        // Lấy lại dữ liệu mới nhất để trả về
        Budget updatedBudget = docRef.get().get().toObject(Budget.class);
        updatedBudget.setBudgetId(budgetId);
        return BudgetResponseDTO.fromModel(updatedBudget);
    }

    // --- 4. XÓA NGÂN SÁCH ---
    public void deleteBudget(String budgetId, String userId) {
        db.collection(COL_USERS).document(userId)
                .collection(COL_BUDGETS).document(budgetId).delete();
    }
}