package com.example.personalfinancebackend.service;

import com.example.personalfinancebackend.model.Wallet;
import com.example.personalfinancebackend.model.dto.WalletDTO;
import com.example.personalfinancebackend.model.dto.WalletListResponseDTO;
import com.example.personalfinancebackend.model.dto.WalletRequestDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private Firestore db;

    private static final String COL_USERS = "users";
    private static final String COL_WALLETS = "wallets";
    private static final String COL_TRANSACTIONS = "transactions";

    // --- 1. LẤY DANH SÁCH VÍ ---
    public WalletListResponseDTO getWalletsByUserId(String userId) throws ExecutionException, InterruptedException {
        // Query: users/{userId}/wallets
        CollectionReference walletsRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS);
        ApiFuture<QuerySnapshot> query = walletsRef.get();

        List<WalletDTO> dtos = new ArrayList<>();
        for (DocumentSnapshot doc : query.get().getDocuments()) {
            Wallet wallet = doc.toObject(Wallet.class);
            if (wallet != null) {
                // Đôi khi toObject không map được ID nếu trường tên id khác
                wallet.setWalletId(doc.getId());
                dtos.add(WalletDTO.fromModel(wallet));
            }
        }
        return new WalletListResponseDTO(dtos);
    }

    // --- 2. TẠO VÍ MỚI ---
    public WalletDTO createWallet(WalletRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        // Tạo document mới tự sinh ID
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document();

        Wallet wallet = new Wallet();
        wallet.setWalletId(docRef.getId());
        wallet.setUserId(userId);
        wallet.setName(dto.getName());
        wallet.setBalance(dto.getInitialBalance() != null ? dto.getInitialBalance().doubleValue() : 0.0);

        // Ghi vào Firestore
        ApiFuture<WriteResult> result = docRef.set(wallet);
        result.get(); // Đợi xong

        return WalletDTO.fromModel(wallet);
    }

    // --- 3. SỬA VÍ (Đổi tên) ---
    public WalletDTO updateWallet(String walletId, WalletRequestDTO dto, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document(walletId);

        // Kiểm tra tồn tại
        DocumentSnapshot snap = docRef.get().get();
        if (!snap.exists()) {
            throw new IllegalArgumentException("Không tìm thấy ví");
        }

        // Cập nhật tên
        ApiFuture<WriteResult> update = docRef.update("name", dto.getName());
        update.get();

        // Trả về object đã update
        Wallet updatedWallet = snap.toObject(Wallet.class);
        updatedWallet.setWalletId(walletId);
        updatedWallet.setName(dto.getName()); // Cập nhật lại tên mới để trả về
        return WalletDTO.fromModel(updatedWallet);
    }

    // --- 4. XÓA VÍ ---
    public void deleteWallet(String walletId, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COL_USERS).document(userId).collection(COL_WALLETS).document(walletId);

        // Kiểm tra ràng buộc: Có giao dịch nào dùng ví này không?
        Query txQuery = db.collection(COL_USERS).document(userId).collection(COL_TRANSACTIONS)
                .whereEqualTo("walletId", walletId).limit(1);

        if (!txQuery.get().get().isEmpty()) {
            throw new IllegalArgumentException("Không thể xóa: Ví đang được sử dụng bởi các giao dịch.");
        }

        // Xóa
        docRef.delete().get();
    }
}