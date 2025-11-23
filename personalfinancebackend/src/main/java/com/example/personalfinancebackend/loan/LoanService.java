package com.example.personalfinancebackend.loan;

import com.example.personalfinancebackend.loan.dto.*;
import com.example.personalfinancebackend.model.Loan;
import com.example.personalfinancebackend.model.enums.LoanStatus;
import com.example.personalfinancebackend.model.enums.LoanType;
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
public class LoanService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_LOANS = "loans";
    private static final String COL_WALLETS = "wallets";
    private static final String COL_TRANSACTIONS = "transactions";

    // --- 1. TẠO KHOẢN VAY/NỢ ---
    public LoanResponseDTO createLoan(LoanRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_LOANS).document();

        Loan loan = new Loan();
        loan.setLoanId(docRef.getId());
        loan.setUserId(userId);
        loan.setType(dto.getType());
        loan.setPersonName(dto.getPersonName());
        loan.setAmount(dto.getAmount().doubleValue());
        loan.setRemainingAmount(dto.getAmount().doubleValue()); // Mới tạo thì còn lại = gốc
        loan.setDueDate(dto.getDueDate().toString());
        loan.setStatus(LoanStatus.unpaid);

        docRef.set(loan).get();
        return LoanResponseDTO.fromModel(loan);
    }

    // --- 2. LẤY DANH SÁCH ---
    public List<LoanResponseDTO> getLoans(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = db.collection(COL_USERS).document(userId).collection(COL_LOANS).get();
        List<LoanResponseDTO> result = new ArrayList<>();
        for (DocumentSnapshot doc : query.get().getDocuments()) {
            Loan loan = doc.toObject(Loan.class);
            if (loan != null) {
                loan.setLoanId(doc.getId());
                result.add(LoanResponseDTO.fromModel(loan));
            }
        }
        return result;
    }

    // --- 3. GHI NHẬN TRẢ NỢ / THU NỢ (Transaction 3 bước) ---
    public LoanResponseDTO recordPayment(String loanId, LoanPaymentDTO dto, String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference loanRef = db.collection(COL_USERS).document(userId).collection(COL_LOANS).document(loanId);
        DocumentReference walletRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document(dto.getWalletId());
        DocumentReference txRef = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS).document();

        // CHẠY TRANSACTION
        ApiFuture<Loan> transactionResult = db.runTransaction(t -> {
            DocumentSnapshot loanSnap = t.get(loanRef).get();
            DocumentSnapshot walletSnap = t.get(walletRef).get();

            if (!loanSnap.exists() || !walletSnap.exists()) {
                throw new IllegalArgumentException("Khoản vay hoặc Ví không tồn tại");
            }

            Loan loan = loanSnap.toObject(Loan.class);
            Double walletBal = walletSnap.getDouble("balance");
            if (walletBal == null) walletBal = 0.0;

            Double paymentAmount = dto.getAmount().doubleValue();

            // Logic xác định Tăng/Giảm ví
            String txType;
            String txDesc;
            double newWalletBal;

            if (loan.getType() == LoanType.debt) {
                // Tôi NỢ -> Tôi TRẢ -> Tiền GIẢM (Expense)
                if (walletBal < paymentAmount) throw new IllegalArgumentException("Số dư ví không đủ trả nợ");
                newWalletBal = walletBal - paymentAmount;
                txType = "EXPENSE";
                txDesc = "Trả nợ cho " + loan.getPersonName();
            } else {
                // Tôi CHO VAY -> Tôi THU -> Tiền TĂNG (Income)
                newWalletBal = walletBal + paymentAmount;
                txType = "INCOME";
                txDesc = "Thu nợ từ " + loan.getPersonName();
            }

            // Cập nhật Loan
            double newRemaining = loan.getRemainingAmount() - paymentAmount;
            if (newRemaining < 0) throw new IllegalArgumentException("Số tiền trả vượt quá số nợ còn lại");

            loan.setRemainingAmount(newRemaining);
            if (newRemaining == 0) loan.setStatus(LoanStatus.paid);

            // Tạo Giao dịch
            Map<String, Object> txData = new HashMap<>();
            txData.put("transactionId", txRef.getId());
            txData.put("walletId", dto.getWalletId());
            txData.put("categoryId", dto.getCategoryId());
            txData.put("amount", paymentAmount);
            txData.put("type", txType);
            txData.put("transactionDate", LocalDateTime.now().toString());
            txData.put("description", txDesc);

            // Ghi đè
            t.update(walletRef, "balance", newWalletBal);
            t.set(loanRef, loan); // Update toàn bộ object loan
            t.set(txRef, txData);

            return loan;
        });

        return LoanResponseDTO.fromModel(transactionResult.get());
    }

    // --- 4. SỬA KHOẢN VAY (Chỉ sửa thông tin, cập nhật lại gốc) ---
    public LoanResponseDTO updateLoan(String loanId, LoanRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        DocumentReference loanRef = db.collection(COL_USERS).document(userId).collection(COL_LOANS).document(loanId);

        return db.runTransaction(t -> {
            Loan loan = t.get(loanRef).get().toObject(Loan.class);
            if (loan == null) throw new IllegalArgumentException("Không tìm thấy");

            // Tính số tiền đã trả
            double paidAmount = loan.getAmount() - loan.getRemainingAmount();
            double newAmount = dto.getAmount().doubleValue();

            if (newAmount < paidAmount) throw new IllegalArgumentException("Số tiền gốc mới nhỏ hơn số đã trả");

            loan.setPersonName(dto.getPersonName());
            loan.setDueDate(dto.getDueDate().toString());
            loan.setType(dto.getType());
            loan.setAmount(newAmount);
            loan.setRemainingAmount(newAmount - paidAmount);

            if (loan.getRemainingAmount() == 0) loan.setStatus(LoanStatus.paid);
            else loan.setStatus(LoanStatus.unpaid);

            t.set(loanRef, loan);
            return LoanResponseDTO.fromModel(loan);
        }).get();
    }

    // --- 5. ĐÁNH DẤU ĐÃ TRẢ (Thủ công) ---
    public LoanResponseDTO markAsPaid(String loanId, String userId) throws ExecutionException, InterruptedException {
        DocumentReference loanRef = db.collection(COL_USERS).document(userId).collection(COL_LOANS).document(loanId);

        // Update field trực tiếp
        loanRef.update("remainingAmount", 0.0, "status", LoanStatus.paid.toString()).get();

        // Lấy lại để trả về
        Loan loan = loanRef.get().get().toObject(Loan.class);
        loan.setLoanId(loanId);
        return LoanResponseDTO.fromModel(loan);
    }
}