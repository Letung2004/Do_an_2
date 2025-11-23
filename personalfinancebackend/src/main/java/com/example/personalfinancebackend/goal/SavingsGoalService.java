package com.example.personalfinancebackend.goal;

import com.example.personalfinancebackend.goal.dto.*;
import com.example.personalfinancebackend.model.SavingsGoal;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class SavingsGoalService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_GOALS = "savings_goals";
    private static final String COL_WALLETS = "wallets";
    private static final String COL_TRANSACTIONS = "transactions";

    // --- 1. TẠO MỤC TIÊU ---
    public SavingsGoalResponseDTO createGoal(SavingsGoalRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_GOALS).document();

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalId(docRef.getId());
        goal.setUserId(userId);
        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount().doubleValue());
        goal.setCurrentAmount(0.0);
        goal.setTargetDate(dto.getTargetDate().toString());
        goal.setCategoryId(dto.getCategoryId()); // Có thể null

        docRef.set(goal).get();
        return SavingsGoalResponseDTO.fromModel(goal);
    }

    // --- 2. LẤY DANH SÁCH ---
    public List<SavingsGoalResponseDTO> getGoals(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = db.collection(COL_USERS).document(userId).collection(COL_GOALS).get();
        List<SavingsGoalResponseDTO> result = new ArrayList<>();
        for (DocumentSnapshot doc : query.get().getDocuments()) {
            SavingsGoal goal = doc.toObject(SavingsGoal.class);
            if (goal != null) {
                goal.setGoalId(doc.getId());
                result.add(SavingsGoalResponseDTO.fromModel(goal));
            }
        }
        return result;
    }

    // --- 3. NẠP TIỀN VÀO MỤC TIÊU (Transaction 3 bước) ---
    public SavingsGoalResponseDTO addMoneyToGoal(String goalId, AddMoneyToGoalRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference goalRef = db.collection(COL_USERS).document(userId).collection(COL_GOALS).document(goalId);
        DocumentReference walletRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document(dto.getWalletId());
        DocumentReference txRef = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS).document();

        // CHẠY TRANSACTION
        ApiFuture<SavingsGoal> transactionResult = db.runTransaction(t -> {
            // A. Lấy dữ liệu Goal và Wallet
            DocumentSnapshot goalSnap = t.get(goalRef).get();
            DocumentSnapshot walletSnap = t.get(walletRef).get();

            if (!goalSnap.exists() || !walletSnap.exists()) {
                throw new IllegalArgumentException("Mục tiêu hoặc Ví không tồn tại");
            }

            Double walletBal = walletSnap.getDouble("balance");
            if (walletBal == null) walletBal = 0.0;
            Double goalCurrent = goalSnap.getDouble("currentAmount");
            if (goalCurrent == null) goalCurrent = 0.0;

            Double amount = dto.getAmount().doubleValue();

            // B. Kiểm tra số dư Ví
            if (walletBal < amount) {
                throw new IllegalArgumentException("Số dư ví không đủ");
            }

            // C. Tính toán
            double newWalletBal = walletBal - amount;
            double newGoalCurrent = goalCurrent + amount;

            // D. Tạo Giao dịch CHI
            Map<String, Object> txData = new HashMap<>();
            txData.put("transactionId", txRef.getId());
            txData.put("walletId", dto.getWalletId());
            txData.put("categoryId", dto.getCategoryId());
            txData.put("amount", amount);
            txData.put("type", "EXPENSE");
            txData.put("transactionDate", LocalDateTime.now().toString());
            txData.put("description", "Nạp tiền mục tiêu: " + goalSnap.getString("name"));

            // E. Ghi đè (Update)
            t.update(walletRef, "balance", newWalletBal);
            t.update(goalRef, "currentAmount", newGoalCurrent);
            t.set(txRef, txData);

            // Trả về Goal đã update
            SavingsGoal updatedGoal = goalSnap.toObject(SavingsGoal.class);
            updatedGoal.setCurrentAmount(newGoalCurrent);
            return updatedGoal;
        });

        return SavingsGoalResponseDTO.fromModel(transactionResult.get());
    }

    // --- 4. RÚT TIỀN KHỎI MỤC TIÊU (Transaction 3 bước) ---
    public SavingsGoalResponseDTO withdrawMoneyFromGoal(String goalId, WithdrawFromGoalRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference goalRef = db.collection(COL_USERS).document(userId).collection(COL_GOALS).document(goalId);
        DocumentReference walletRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document(dto.getWalletId());
        DocumentReference txRef = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS).document();

        ApiFuture<SavingsGoal> transactionResult = db.runTransaction(t -> {
            DocumentSnapshot goalSnap = t.get(goalRef).get();
            DocumentSnapshot walletSnap = t.get(walletRef).get();

            if (!goalSnap.exists() || !walletSnap.exists()) throw new IllegalArgumentException("Lỗi dữ liệu");

            Double walletBal = walletSnap.getDouble("balance");
            Double goalCurrent = goalSnap.getDouble("currentAmount");
            Double amount = dto.getAmount().doubleValue();

            // Kiểm tra số dư Mục tiêu
            if (goalCurrent < amount) {
                throw new IllegalArgumentException("Số tiền trong mục tiêu không đủ");
            }

            double newWalletBal = walletBal + amount; // Cộng lại vào ví
            double newGoalCurrent = goalCurrent - amount; // Trừ khỏi mục tiêu

            // Tạo Giao dịch THU
            Map<String, Object> txData = new HashMap<>();
            txData.put("transactionId", txRef.getId());
            txData.put("walletId", dto.getWalletId());
            txData.put("categoryId", dto.getCategoryId());
            txData.put("amount", amount);
            txData.put("type", "INCOME");
            txData.put("transactionDate", LocalDateTime.now().toString());
            txData.put("description", "Rút tiền mục tiêu: " + goalSnap.getString("name"));

            t.update(walletRef, "balance", newWalletBal);
            t.update(goalRef, "currentAmount", newGoalCurrent);
            t.set(txRef, txData);

            SavingsGoal updatedGoal = goalSnap.toObject(SavingsGoal.class);
            updatedGoal.setCurrentAmount(newGoalCurrent);
            return updatedGoal;
        });

        return SavingsGoalResponseDTO.fromModel(transactionResult.get());
    }

    // --- 5. SỬA MỤC TIÊU (Mới thêm) ---
    public SavingsGoalResponseDTO updateGoal(String goalId, SavingsGoalRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_GOALS).document(goalId);
        DocumentSnapshot snap = docRef.get().get();

        if (!snap.exists()) {
            throw new IllegalArgumentException("Mục tiêu không tồn tại");
        }

        // Chỉ cập nhật các thông tin cơ bản, KHÔNG cập nhật currentAmount (vì phải nạp/rút)
        docRef.update(
                "name", dto.getName(),
                "targetAmount", dto.getTargetAmount().doubleValue(),
                "targetDate", dto.getTargetDate().toString(),
                "categoryId", dto.getCategoryId()
        ).get();

        // Lấy dữ liệu mới nhất
        SavingsGoal updatedGoal = docRef.get().get().toObject(SavingsGoal.class);
        updatedGoal.setGoalId(goalId);
        return SavingsGoalResponseDTO.fromModel(updatedGoal);
    }

    // --- 6. XÓA MỤC TIÊU (Mới thêm) ---
    public void deleteGoal(String goalId, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_GOALS).document(goalId);
        DocumentSnapshot snap = docRef.get().get();

        if (!snap.exists()) {
            throw new IllegalArgumentException("Mục tiêu không tồn tại");
        }

        // RÀNG BUỘC: Chỉ cho phép xóa nếu tiền trong mục tiêu = 0
        Double currentAmount = snap.getDouble("currentAmount");
        if (currentAmount != null && currentAmount > 0) {
            throw new IllegalArgumentException("Không thể xóa mục tiêu đang có tiền (" + currentAmount + "). Vui lòng rút hết tiền về ví trước.");
        }

        // Xóa
        docRef.delete().get();
    }
}