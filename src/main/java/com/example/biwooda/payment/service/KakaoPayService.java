package com.example.biwooda.payment.service;

import com.example.biwooda.payment.exception.AlreadyBorrowedException;
import com.example.biwooda.payment.model.KakaoApproveResponse;
import com.example.biwooda.payment.model.KakaoPayItemInfo;
import com.example.biwooda.payment.model.KakaoPayRequest;
import com.example.biwooda.payment.model.KakaoReadyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.http.HttpHeaders;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KakaoPayService {
    private final MakePayRequest makePayRequest;
    private final KakaoFirestoreService firestoreService;
    private static final int UMBRELLA_PRICE = 10000;


    @Autowired
    public KakaoPayService(MakePayRequest makePayRequest, KakaoFirestoreService firestoreService) {
        this.makePayRequest = makePayRequest;
        this.firestoreService = firestoreService;
    }

    // KakaoPay admin key
    @Value("${pay.kakao-admin-key}")
    private String adminKey;

    // Initialize payment and return the payment page redirect URL
    // Request: item information
    // Response: transaction ID (tid) and payment redirect URL
    @Transactional
    public KakaoReadyResponse getRedirectUrl(String idToken, KakaoPayItemInfo item) throws  Exception{
        // Retrieve user UID from Firebase ID Token
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();

        // Check whether the user already has an active rental
        CompletableFuture<Boolean> future = firestoreService.isAlreadyBorrow(uid);
        boolean userExists = future.get(); // Wait for the async check to complete

        if (userExists) {
            throw new AlreadyBorrowedException();
        }

        // Proceed with payment initialisation
        HttpHeaders headers = new HttpHeaders();

        // Configure request headers
        String auth = "KakaoAK " + adminKey;
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", auth);

        // Build request body
        KakaoPayRequest request = makePayRequest.getReadyRequest(uid, item);

        // Combine headers and body for RestTemplate
        HttpEntity<MultiValueMap<String, String>> urlRequest = new HttpEntity<>(request.getParameters(), headers);
        System.out.println(urlRequest);

        // Send request to KakaoPay and map the response to DTO
        RestTemplate rt = new RestTemplate();
        KakaoReadyResponse response = rt.postForObject(request.getUrl(), urlRequest, KakaoReadyResponse.class);
        System.out.println(response);

        // Store tid in Firestore
        assert response != null;
        firestoreService.addTid(uid, response.getTid(), response);

        return response;
    }

    // Handle payment approval request from the client
    @Transactional
    public KakaoApproveResponse getApprove(String pgToken, String idToken) throws Exception {
        // Retrieve user uid and tid
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        String tid = firestoreService.getTid(uid);

        HttpHeaders headers = new HttpHeaders();

        // Configure request headers 
        String auth = "KakaoAK " + adminKey;
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", auth);

        // Build request body 
        KakaoPayRequest request=makePayRequest.getApproveRequest(uid, tid, pgToken);


        // Combine headers and body for RestTemplate
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request.getParameters(), headers);

        // Send approval request
        RestTemplate rt = new RestTemplate();
        KakaoApproveResponse approveData = rt.postForObject(request.getUrl(), requestEntity, KakaoApproveResponse.class);

        // Persist approved payment data
        assert approveData != null;
        firestoreService.addApproveData(uid, pgToken, approveData);
        firestoreService.saveSid(uid, approveData);

        return approveData;
    }

    // Handle payment cancellation
    public void cancelReady(String idToken) throws Exception {
        // Retrieve user uid and tid
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        String tid = firestoreService.getTid(uid);

        firestoreService.updateReadyCancel(uid, tid);
    }

    // Handle payment failure
    public void failReady(String idToken) throws Exception {
        // Retrieve user uid and tid
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        String tid = firestoreService.getTid(uid);

        firestoreService.updateReadyFailed(uid, tid);
    }
}
