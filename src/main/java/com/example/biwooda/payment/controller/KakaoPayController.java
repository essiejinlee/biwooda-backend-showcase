package com.example.biwooda.payment.controller;

import com.example.biwooda.payment.exception.AlreadyBorrowedException;
import com.example.biwooda.payment.model.KakaoApproveResponse;
import com.example.biwooda.payment.model.KakaoBaseResponse;
import com.example.biwooda.payment.model.KakaoPayItemInfo;
import com.example.biwooda.payment.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class KakaoPayController {
    private final KakaoPayService kakaoPayService;

    public KakaoPayController(KakaoPayService kakaoPayService) {
        this.kakaoPayService = kakaoPayService;
    }

    // Request: item information -> Response: payment page (redirect URL)
    @PostMapping("/ready")
    public ResponseEntity<?> getRedirectUrl(@RequestHeader("Authorization") String authorization, @RequestBody KakaoPayItemInfo item){
        String idToken = authorization.replace("Bearer ", "");
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(kakaoPayService.getRedirectUrl(idToken, item));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    // Result of the ready request (receives pg_token)
    @GetMapping("/success/{id}")
    public ResponseEntity<?> afterGetRedirectUrl(@PathVariable("id") String idToken, @RequestParam("pg_token") String pgToken){
        try{
            KakaoApproveResponse kakaoApprove = kakaoPayService.getApprove(pgToken, idToken);
            return ResponseEntity.status(HttpStatus.OK).body(kakaoApprove);
        }catch (AlreadyBorrowedException e){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    // Payment cancelled by the user during the payment process
    @GetMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable("id") String idToken){
        try {
            kakaoPayService.cancelReady(idToken);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), "사용자가 결제를 취소하였습니다."));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    // Payment failed
    @GetMapping("/fail/{id}")
    public ResponseEntity<?> fail(@PathVariable("id") String idToken){
        try {
            kakaoPayService.failReady(idToken);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), "결제 실패"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

}
