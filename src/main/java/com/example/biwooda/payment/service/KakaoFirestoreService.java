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

    // Check wheter the user already has an active rental
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

    // Register payment transaction ID (tid)
    public void addTid(String uid, String tid, KakaoReadyResponse readyData) throws ExecutionException, InterruptedException {
        // Retrieve the user's purchase collection
        DocumentReference user = firestore.collection("users").document(uid);
        CollectionReference list = user.collection("purchase");

        // Store initial payment data
        Map<String, Object> contents = new HashMap<>();
        contents.put("created_at", readyData.getCreated_at());
        contents.put("status", "ready");
        DocumentReference purchase = list.document(tid);

        // Blocking call to ensure write completion
        ApiFuture<WriteResult> result = purchase.set(contents);
        result.get();
    }

    // Store approved payment details
    public void addApproveData(String uid, String pgToken, KakaoApproveResponse approveData) throws ExecutionException, InterruptedException {
        // Retrieve the user's purchase document using tid
        String tid = approveData.getTid();
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        // Add approved payment information
        Map<String, Object> contents = new HashMap<>();
        contents.put("sid", approveData.getSid());
        contents.put("pg_token", pgToken);
        contents.put("created_at", approveData.getCreated_at());
        contents.put("approved_at", approveData.getApproved_at());
        contents.put("status", "approved");
        contents.put("item_name", approveData.getItem_name());

        // Blocking call to ensure update completion
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    // Register rental information after payment approval
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
            // Wait for the asynchronous operation to complete
            future.get();
            System.out.println("Data saved successfully.");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to save data: " + e.getMessage());
            throw new RuntimeException("Failed to save data: " + e.getMessage(), e);
        }
    }

    // Update payment status to cancelled
    public void updateReadyCancel(String uid, String tid) throws ExecutionException, InterruptedException {
        // Retrieve the user's purchase document using tid
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        Map<String, Object> contents = new HashMap<>();
        contents.put("status", "canceled");

        // Blocking call to ensure update completion
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    // Update payment status to failed
    public void updateReadyFailed(String uid, String tid) throws ExecutionException, InterruptedException {
        // Retrieve the user's purchase document using ti
        DocumentReference user = firestore.collection("users").document(uid);
        DocumentReference purchase = user.collection("purchase").document(tid);

        Map<String, Object> contents = new HashMap<>();
        contents.put("status", "failed");

        // Blocking call to ensure update completion
        ApiFuture<WriteResult> result = purchase.update(contents);
        result.get();
    }

    // Retrieve the tid with status 'ready'
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
