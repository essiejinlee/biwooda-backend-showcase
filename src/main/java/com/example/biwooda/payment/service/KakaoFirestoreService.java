package com.example.biwooda.payment.service;

import com.example.biwooda.payment.model.KakaoApproveResponse;
import com.example.biwooda.payment.model.KakaoReadyResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class KakaoFirestoreService {
    private final Firestore firestore;

    public KakaoFirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    //이미 대여중인 사용자인지 아닌지 파악
    public CompletableFuture<Boolean> isAlreadyBorrow(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("borrows").child(uid);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                future.complete(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    // 결제 고유 번호(tid) 등록
    public void addTid(String uid, String tid, KakaoReadyResponse readyData) throws ExecutionException, InterruptedException {
        // 해당 사용자의 구매 컬렉션 가져오기
        DocumentReference user = firestore.collection("users").document(uid);
        CollectionReference list = user.collection("purchase");

        // 결제 고유 번호 추가
        Map<String, Object> contents = new HashMap<>();
        contents.put("created_at", readyData.getCreated_at());
        contents.put("status", "ready");
        DocumentReference purchase = list.document(tid);

        // 블록킹 호출로, 성공 여부 확인
        ApiFuture<WriteResult> result = purchase.set(contents);
        result.get();
    }

    // 결제 완료된 상품 정보 등록
    public void addApproveData(String uid, String pgToken, KakaoApproveResponse approveData) throws ExecutionException, InterruptedException {
        // 해당 사용자의 특정 구매 문서 가져오기: 결제 고유번호로
        String tid = approveData.getTid();
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        // 결제 상품 정보 추가
        Map<String, Object> contents = new HashMap<>();
        contents.put("sid", approveData.getSid());
        contents.put("pg_token", pgToken);
        contents.put("created_at", approveData.getCreated_at());
        contents.put("approved_at", approveData.getApproved_at());
        contents.put("status", "approved");
        contents.put("item_name", approveData.getItem_name());

        // 블록킹 호출로, 성공 여부 확인
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    //대여 목록에 등록
    public void saveSid(String uid, KakaoApproveResponse approveData) throws Exception{
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("borrows").child(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("sid", approveData.getSid());
        updates.put("tid", approveData.getTid());
        updates.put("amount", approveData.getAmount().getTotal());
        updates.put("itemName", approveData.getItem_name());
        updates.put("approved_at", approveData.getApproved_at());

        ApiFuture<Void> future = ref.updateChildrenAsync(updates);

        try {
            // 비동기 작업이 완료될 때까지 대기
            future.get();
            System.out.println("Data saved successfully.");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to save data: " + e.getMessage());
            throw new RuntimeException("Failed to save data: " + e.getMessage(), e);
        }
    }

    // 결제 취소 - 상태 업데이트
    public void updateReadyCancel(String uid, String tid) throws ExecutionException, InterruptedException {
        // 해당 사용자의 특정 구매 문서 가져오기: 결제 고유번호로
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        // 결제 상품 정보 추가
        Map<String, Object> contents = new HashMap<>();
        contents.put("status", "canceled");

        // 블록킹 호출로, 성공 여부 확인
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    // 결제 실패 - 상태 업데이트
    public void updateReadyFailed(String uid, String tid) throws ExecutionException, InterruptedException {
        // 해당 사용자의 특정 구매 문서 가져오기: 결제 고유번호로
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        // 결제 상품 정보 추가
        Map<String, Object> contents = new HashMap<>();
        contents.put("status", "failed");

        // 블록킹 호출로, 성공 여부 확인
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    public String getTid(String uid) throws ExecutionException, InterruptedException {
        CollectionReference transactions = firestore.collection("users").document(uid).collection("purchase");
        ApiFuture<QuerySnapshot> future = transactions.whereEqualTo("status", "ready").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).getId();
        }
        throw new IllegalArgumentException("No transaction with status 'ready' found for user " + uid);
    }
}
