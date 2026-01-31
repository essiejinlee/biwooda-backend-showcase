package com.example.biwooda.auth.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.auth.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

// Service for generating Firebase custom tokens
@Service
public class FirebaseCustomService {

    private final Firestore firestore;

    public FirebaseCustomService(Firestore firestore) {
        this.firestore = firestore;
    }

    public String createFirebaseToken(String uid) throws FirebaseAuthException {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            // User exists -> generate a custom token
            return FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setUid(uid);
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                // Create user document in the database
                createUser(uid);
                return FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
            } else
                throw e;
        }
    }

    public void updateUser(String firebaseUid, String email, String name) throws FirebaseAuthException {
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(firebaseUid);
        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
                .setEmail(email)
                .setDisplayName(name);
        UserRecord updatedUser = FirebaseAuth.getInstance().updateUser(updateRequest);
        System.out.println("Updated user: " + updatedUser);
    }

    // Create a user document during sign-up
    public void createUser(String uid) {
        // Create a user document in the "users" collection using the Firebase Auth UID as the key
        DocumentReference doc = firestore.collection("users").document(uid);
        // Contents to be stored in the document
        Map<String, Object> contents = new HashMap<>();
        // Current timestamp
        long currentTimeMillis = System.currentTimeMillis();
        // Format timestamp to Korean local time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String formattedDate = sdf.format(new Date(currentTimeMillis));
        contents.put("created_at", formattedDate);
        ApiFuture<WriteResult> future = doc.set(contents);
        ApiFutures.addCallback(future, new ApiFutureCallback<WriteResult>() {
            @Override
            public void onSuccess(WriteResult result) {
                System.out.println("User document created successfully for UID: " + uid);
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Error adding user document for UID " + uid + ": " + t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    // Merge accounts when the same email is used
    public void linkAccounts(UserRecord existingUser) throws FirebaseAuthException {
        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(existingUser.getUid())
                .setDisplayName(existingUser.getDisplayName());
        FirebaseAuth.getInstance().updateUser(updateRequest);
    }
}
