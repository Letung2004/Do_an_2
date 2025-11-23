package com.example.personalfinancebackend.service;

import com.example.personalfinancebackend.model.Budget;
import com.example.personalfinancebackend.model.User;
import com.example.personalfinancebackend.transaction.TransactionRequestDTO;
import com.example.personalfinancebackend.transaction.TransactionResponseDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class TransactionService {

    @Autowired
    private Firestore db;

    @Autowired
    private NotificationService notificationService; // Inject Service Thông báo

    private static final String COL_USERS = "users";
    private static final String COL_TRANSACTIONS = "transactions";
    private static final String COL_WALLETS = "wallets";
    private static final String COL_BUDGETS = "budgets";

    public TransactionResponseDTO addTransaction(TransactionRequestDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference walletRef = db.collection(COL_USERS).document(userId)
                .collection(COL_WALLETS).document(dto.getWalletId());
        CollectionReference txColRef = db.collection(COL_USERS).document(userId)
                .collection(COL_TRANSACTIONS);

        // Tìm Budget ID (nếu là Chi tiêu)
        String budgetIdToUpdate = null;
        if ("EXPENSE".equalsIgnoreCase(dto.getType())) {
            budgetIdToUpdate = findMatchingBudgetId(userId, dto.getCategoryId(), dto.getTransactionDate());
        }
        final String finalBudgetId = budgetIdToUpdate;

        // --- BẮT ĐẦU TRANSACTION ---
        ApiFuture<TransactionResponseDTO> futureTransaction = db.runTransaction(t -> {
            // A. Kiểm tra Ví
            DocumentSnapshot walletSnap = t.get(walletRef).get();
            if (!walletSnap.exists()) throw new IllegalArgumentException("Ví không tồn tại");

            // B. Cập nhật Ngân sách (Tính toán & Lưu lại)
            if (finalBudgetId != null) {
                DocumentReference budgetRef = db.collection(COL_USERS).document(userId)
                        .collection(COL_BUDGETS).document(finalBudgetId);
                DocumentSnapshot budgetSnap = t.get(budgetRef).get();

                if (budgetSnap.exists()) {
                    Double currentSpent = budgetSnap.getDouble("currentSpent");
                    if (currentSpent == null) currentSpent = 0.0;
                    double newSpent = currentSpent + dto.getAmount().doubleValue();

                    t.update(budgetRef, "currentSpent", newSpent);
                }
            }

            // C. Tạo Giao dịch
            DocumentReference newTxRef = txColRef.document();
            Map<String, Object> txData = new HashMap<>();
            txData.put("transactionId", newTxRef.getId());
            txData.put("walletId", dto.getWalletId());
            txData.put("categoryId", dto.getCategoryId());
            txData.put("amount", dto.getAmount().doubleValue());
            txData.put("type", dto.getType());
            txData.put("transactionDate", dto.getTransactionDate().toString());
            txData.put("description", dto.getDescription());

            // D. Trừ Ví
            Double currentBalance = walletSnap.getDouble("balance");
            if (currentBalance == null) currentBalance = 0.0;
            double amountVal = dto.getAmount().doubleValue();
            double newBalance = "EXPENSE".equalsIgnoreCase(dto.getType()) ? currentBalance - amountVal : currentBalance + amountVal;

            t.set(newTxRef, txData);
            t.update(walletRef, "balance", newBalance);

            // E. Return
            TransactionResponseDTO response = new TransactionResponseDTO();
            response.setTransactionId(newTxRef.getId());
            // ... set other fields
            return response;
        });

        TransactionResponseDTO result = futureTransaction.get(); // Đợi Transaction xong hoàn toàn

        // --- SAU KHI TRANSACTION XONG (GỬI THÔNG BÁO) ---
        // Ta làm ở đây để không block transaction và tránh lỗi network trong transaction
        if (finalBudgetId != null) {
            checkAndNotifyBudget(userId, finalBudgetId);
        }

        return result;
    }

    // Hàm kiểm tra và bắn thông báo (Chạy Async sau transaction)
    private void checkAndNotifyBudget(String userId, String budgetId) {
        try {
            DocumentSnapshot budgetSnap = db.collection(COL_USERS).document(userId)
                    .collection(COL_BUDGETS).document(budgetId).get().get();

            if (budgetSnap.exists()) {
                Double currentSpent = budgetSnap.getDouble("currentSpent");
                Double limit = budgetSnap.getDouble("amountLimit");
                String catName = budgetSnap.getString("categoryName");

                if (currentSpent != null && limit != null && limit > 0) {
                    double percent = (currentSpent / limit) * 100;

                    // Logic Cảnh báo: Nếu vượt quá 80%
                    if (percent >= 80) {
                        // Lấy FCM Token của User
                        DocumentSnapshot userSnap = db.collection(COL_USERS).document(userId).get().get();
                        String fcmToken = userSnap.getString("fcmToken");

                        // Gọi Service gửi thông báo
                        notificationService.sendBudgetAlert(fcmToken, catName, percent, limit);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra ngân sách sau giao dịch: " + e.getMessage());
        }
    }

    // ... (Giữ nguyên hàm findMatchingBudgetId và getTransactions như cũ) ...
    private String findMatchingBudgetId(String userId, String categoryId, LocalDateTime transactionDate)
            throws ExecutionException, InterruptedException {
        Query query = db.collection(COL_USERS).document(userId).collection(COL_BUDGETS)
                .whereEqualTo("categoryId", categoryId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        String txDateStr = transactionDate.toString();

        for (DocumentSnapshot doc : documents) {
            String startDate = doc.getString("startDate");
            String endDate = doc.getString("endDate");
            if (startDate != null && endDate != null) {
                if (txDateStr.compareTo(startDate) >= 0 && txDateStr.compareTo(endDate) <= 0) {
                    return doc.getId();
                }
            }
        }
        return null;
    }

    public List<Map<String, Object>> getTransactions(String userId, String walletId, String categoryId)
            throws ExecutionException, InterruptedException {
        CollectionReference txColRef = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS);
        Query query = txColRef;
        if (walletId != null) query = query.whereEqualTo("walletId", walletId);
        if (categoryId != null) query = query.whereEqualTo("categoryId", categoryId);
        query = query.orderBy("transactionDate", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            results.add(document.getData());
        }
        return results;
    }

    public void deleteTransaction(String userId, String transactionId) {
        db.collection(COL_USERS).document(userId)
                .collection(COL_TRANSACTIONS).document(transactionId).delete();
    }
}