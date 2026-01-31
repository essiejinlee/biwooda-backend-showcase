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

    //kakaoPay key
    @Value("${pay.kakao-admin-key}")
    private String adminKey;

    //클라이언트의 결제창을 응답으로 받기 위한 메소드
    //request: 상품 정보 / response: tid(결제 고유 번호) & 결제 URL
    @Transactional
    public KakaoReadyResponse getRedirectUrl(String idToken, KakaoPayItemInfo item) throws  Exception{
        //사용자 uid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "u8N6Q6pYULfUfbo0tlqFw3ET3zq1";

        //이미 대여중인 사용자인지 확인
        CompletableFuture<Boolean> future = firestoreService.isAlreadyBorrow(uid);
        boolean userExists = future.get(); // 여기서 CompletableFuture를 완료할 때까지 대기하고 결과를 가져옵니다.

        if (userExists) {
            throw new AlreadyBorrowedException();
        }

        //아니라면, 계속 진행
        HttpHeaders headers = new HttpHeaders();

        //요청 header
        String auth = "KakaoAK " + adminKey;
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", auth);

        //요청 body
        KakaoPayRequest request = makePayRequest.getReadyRequest(uid, item);

        //Header와 Body 합쳐서 RestTemplate로 보내기 위한 밑작업
        HttpEntity<MultiValueMap<String, String>> urlRequest = new HttpEntity<>(request.getParameters(), headers);
        System.out.println(urlRequest);

        /** RestTemplate로 Response 받아와서 DTO로 변환후 return */
        RestTemplate rt = new RestTemplate();
        KakaoReadyResponse response = rt.postForObject(request.getUrl(), urlRequest, KakaoReadyResponse.class);
        System.out.println(response);

        //Firestore에 TID 저장
        assert response != null;
        firestoreService.addTid(uid, response.getTid(), response);

        return response;
    }

    //클라이언트의 결제 승인 요청 메소드
    @Transactional
    public KakaoApproveResponse getApprove(String pgToken, String idToken) throws Exception {
        //사용자 uid, tid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "u8N6Q6pYULfUfbo0tlqFw3ET3zq1";
        String tid = firestoreService.getTid(uid);

        HttpHeaders headers = new HttpHeaders();

        //요청 header
        String auth = "KakaoAK " + adminKey;
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", auth);

        /** 요청 Body */
        KakaoPayRequest request=makePayRequest.getApproveRequest(uid, tid, pgToken);


        /** Header와 Body 합쳐서 RestTemplate로 보내기 위한 밑작업 */
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request.getParameters(), headers);

        // 요청 보내기
        RestTemplate rt = new RestTemplate();
        KakaoApproveResponse approveData = rt.postForObject(request.getUrl(), requestEntity, KakaoApproveResponse.class);

        //Firestore에 결제 정보 저장
        assert approveData != null;
        firestoreService.addApproveData(uid, pgToken, approveData);
        firestoreService.saveSid(uid, approveData);

        return approveData;
    }

    //결제 취소
    public void cancelReady(String idToken) throws Exception {
        //사용자 uid, tid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "u8N6Q6pYULfUfbo0tlqFw3ET3zq1";
        String tid = firestoreService.getTid(uid);

        firestoreService.updateReadyCancel(uid, tid);
    }

    //결제 실패
    public void failReady(String idToken) throws Exception {
        //사용자 uid, tid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "u8N6Q6pYULfUfbo0tlqFw3ET3zq1";
        String tid = firestoreService.getTid(uid);

        firestoreService.updateReadyFailed(uid, tid);
    }
}
