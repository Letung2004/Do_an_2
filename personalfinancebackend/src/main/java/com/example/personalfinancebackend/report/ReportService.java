package com.example.personalfinancebackend.report;

import com.example.personalfinancebackend.model.Category;
import com.example.personalfinancebackend.model.Transaction;
import com.example.personalfinancebackend.report.dto.CategoryReportDTO;
import com.example.personalfinancebackend.report.dto.DailySummaryReportDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_TRANSACTIONS = "transactions";
    private static final String COL_CATEGORIES = "categories"; // Custom
    private static final String COL_DEFAULT_CATEGORIES = "default_categories"; // Default

    /**
     * Báo cáo Chi tiêu theo Danh mục (Tính toán trên Java)
     */
    public List<CategoryReportDTO> getExpenseReport(String userId, LocalDate startDate, LocalDate endDate)
            throws ExecutionException, InterruptedException {

        // 1. Lấy tất cả giao dịch trong khoảng ngày
        List<Transaction> transactions = getTransactionsInRange(userId, startDate, endDate);

        // 2. Lọc chỉ lấy EXPENSE
        List<Transaction> expenses = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.toList());

        // 3. Tính tổng tiền cho từng Category (Map<CategoryId, TotalAmount>)
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // 4. Map CategoryId sang CategoryName và tạo DTO
        List<CategoryReportDTO> report = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String catId = entry.getKey();
            BigDecimal total = BigDecimal.valueOf(entry.getValue());
            grandTotal = grandTotal.add(total);

            // Lấy tên danh mục (Cần cache hoặc query tối ưu, ở đây demo query đơn giản)
            String catName = getCategoryName(userId, catId);

            report.add(new CategoryReportDTO(catName, total));
        }

        // 5. Tính phần trăm
        if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            for (CategoryReportDTO item : report) {
                double percentage = item.getTotalAmount()
                        .divide(grandTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                item.setPercentage(percentage);
            }
        }

        // Sắp xếp giảm dần theo số tiền
        report.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        return report;
    }

    /**
     * Báo cáo Thu/Chi theo Ngày
     */
    public List<DailySummaryReportDTO> getDailySummaryReport(String userId, LocalDate startDate, LocalDate endDate)
            throws ExecutionException, InterruptedException {

        List<Transaction> transactions = getTransactionsInRange(userId, startDate, endDate);

        // Group by Date (yyyy-MM-dd)
        Map<String, List<Transaction>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDate().substring(0, 10))); // Lấy 10 ký tự đầu

        List<DailySummaryReportDTO> report = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : byDate.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            List<Transaction> txs = entry.getValue();

            double income = txs.stream().filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double expense = txs.stream().filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();

            report.add(new DailySummaryReportDTO(date, BigDecimal.valueOf(income), BigDecimal.valueOf(expense)));
        }

        // Sắp xếp theo ngày tăng dần
        report.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        return report;
    }

    // --- Hàm phụ trợ ---

    private List<Transaction> getTransactionsInRange(String userId, LocalDate startDate, LocalDate endDate)
            throws ExecutionException, InterruptedException {

        String startStr = startDate.toString();
        String endStr = endDate.toString() + "T23:59:59"; // Bao gồm cuối ngày

        // Query Firestore (Lọc theo ngày)
        // Lưu ý: transactionDate trong Firestore phải là String chuẩn ISO
        Query query = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS)
                .whereGreaterThanOrEqualTo("transactionDate", startStr)
                .whereLessThanOrEqualTo("transactionDate", endStr);

        ApiFuture<QuerySnapshot> snapshot = query.get();
        return snapshot.get().toObjects(Transaction.class);
    }

    // Hàm lấy tên danh mục (Có thể cache để tối ưu)
    private String getCategoryName(String userId, String catId) throws ExecutionException, InterruptedException {
        // Thử tìm trong custom trước
        DocumentSnapshot doc = db.collection(COL_USERS).document(userId)
                .collection(COL_CATEGORIES).document(catId).get().get();

        if (doc.exists()) return doc.getString("name");

        // Nếu không có, tìm trong default
        doc = db.collection(COL_DEFAULT_CATEGORIES).document(catId).get().get();
        if (doc.exists()) return doc.getString("name");

        return "Unknown";
    }
}