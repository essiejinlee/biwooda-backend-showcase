package com.example.biwooda.auth.controller;

import com.example.biwooda.auth.service.FirebaseCustomService;
import com.example.biwooda.auth.service.KakaoApiService;
import com.example.biwooda.auth.service.KakaoTokenService;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth/kakao")
public class KakaoAuthController {

    private final FirebaseCustomService firebaseCustomService;
    private final KakaoTokenService kakaoTokenService;
    private final KakaoApiService kakaoApiService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    public KakaoAuthController(FirebaseCustomService firebaseCustomService, KakaoTokenService kakaoTokenService, KakaoApiService kakaoApiService) {
        this.firebaseCustomService = firebaseCustomService;
        this.kakaoTokenService = kakaoTokenService;
        this.kakaoApiService = kakaoApiService;
    }

    @GetMapping("/oauth")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        System.out.println("redirectToKakao called");
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize?client_id="+clientId+"&redirect_uri="+redirectUri+"&response_type=code";
        response.sendRedirect(kakaoLoginUrl);
    }

    @GetMapping("/oauth/callback")
    @ResponseBody
    public ResponseEntity<Map<String, String>> kakaoCallback(String code) {
        try {
            // 카카오로부터 액세스 토큰 요청
            String accessToken = kakaoTokenService.requestAccessToken(code, clientId, redirectUri);
            // 액세스 토큰으로부터 카카오 사용자 ID 추출
            String kakaoUserId = kakaoTokenService.getKakaoUserId(accessToken);
            // 카카오 이름, 이메일 가져오기
            String[] emailAndNickname = kakaoApiService.getEmailAndName(accessToken);
            String email = emailAndNickname[0];
            String nickname = emailAndNickname[1];
            // Firebase 사용자 업데이트
            firebaseCustomService.updateUser(kakaoUserId, email, nickname);
            // 응답 맵 준비
            Map<String, String> response = new HashMap<>();
            response.put("email", email); // 이메일 추가
            response.put("nickname", nickname); // 닉네임 추가

            // 중복 이메일 처리
            try {
                UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(email);
                firebaseCustomService.linkAccounts(existingUser);
                String firebaseToken = firebaseCustomService.createFirebaseToken(existingUser.getUid());
                response.put("firebaseToken", firebaseToken);
                return ResponseEntity.ok(response);
            } catch (FirebaseAuthException e) {
                if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                    String firebaseToken = firebaseCustomService.createFirebaseToken(kakaoUserId);
                    firebaseCustomService.updateUser(kakaoUserId, email, nickname);
                    response.put("firebaseToken", firebaseToken);
                    return ResponseEntity.ok(response);
                } else {
                    throw e;
                }
            }

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "토큰 처리 중 오류 발생: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
