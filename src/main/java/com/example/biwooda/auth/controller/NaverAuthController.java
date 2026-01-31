package com.example.biwooda.auth.controller;

import com.example.biwooda.auth.service.FirebaseCustomService;
import com.example.biwooda.auth.service.NaverStateTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth/naver")
public class NaverAuthController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NaverStateTokenService stateTokenService;
    private final FirebaseCustomService firebaseCustomService;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Autowired
    public NaverAuthController(NaverStateTokenService stateTokenService, FirebaseCustomService firebaseCustomService) {
        this.stateTokenService = stateTokenService;
        this.firebaseCustomService = firebaseCustomService;
    }

    @GetMapping("/oauth")
    public RedirectView naverLogin(HttpSession session) {
        String state = stateTokenService.generateState();
        stateTokenService.saveStateToken(session);
        String stateToken = stateTokenService.getStateToken(session);
        session.setAttribute("state", stateToken);
        String apiURL = "https://nid.naver.com/oauth2.0/authorize?client_id="+clientId+"&response_type=code&redirect_uri="+redirectUri+"&state="+stateToken;
        return new RedirectView(apiURL);
    }

    @ResponseBody
    @GetMapping("/oauth/callback")
    public ResponseEntity<Map<String, String>> naverCallback(@RequestParam String code, @RequestParam String state, HttpSession session) {
        // 세션 또는 저장 공간에 저장된 상태 토큰과 콜백으로 전달받은 상태 토큰을 비교하여 검증
        String storedState = (String) session.getAttribute("state");

        if (!state.equals(storedState)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid state token");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 상태 토큰 검증이 성공하면 인증 코드로 접근 토큰을 요청
        String accessToken = getAccessToken(code, state);

        if (accessToken == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get access token");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String apiUrl = "https://openapi.naver.com/v1/nid/me";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                // System.out.println("Naver API Response: " + responseBody); // 확인용 출력
                String nickname = getNaverNickname(responseBody);
                String email = getNaverEmail(responseBody);
                String uid = getNaverUserID(responseBody);

                firebaseCustomService.updateUser(uid, email, nickname);
                firebaseCustomService.createUser(uid);

                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("nickname", nickname);
                successResponse.put("email", email);
                //return ResponseEntity.ok(successResponse);

                // 중복 이메일 처리
                try {
                    UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(email);
                    firebaseCustomService.linkAccounts(existingUser);
                    String firebaseToken = firebaseCustomService.createFirebaseToken(existingUser.getUid());
                    successResponse.put("firebaseToken", firebaseToken);
                    return ResponseEntity.ok(successResponse);
                } catch (FirebaseAuthException e) {
                    if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                        String firebaseToken = firebaseCustomService.createFirebaseToken(uid);
                        firebaseCustomService.createUser(uid);
                        firebaseCustomService.updateUser(uid, email, nickname);
                        successResponse.put("firebaseToken", firebaseToken);
                        return ResponseEntity.ok(successResponse);
                    } else {
                        throw e;
                    }
                }

            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to fetch user information from Naver API");
                return ResponseEntity.status(response.getStatusCode()).body(errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Firebase custom token");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        String accessToken = (String) session.getAttribute("naverAccessToken");

        if (accessToken != null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No access token found in session");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        boolean success = invalidateAccessToken(accessToken);

        if (!success) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to invalidate access token");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        session.invalidate();

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("success", "Successfully logged out");
        return ResponseEntity.ok(successResponse);
    }

    private String getAccessToken(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=authorization_code" +
                "&state=" + state +
                "&code=" + code;

        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(tokenUrl, String.class);
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getNaverNickname(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode responseNode = jsonNode.get("response");
        if (responseNode != null) {
            JsonNode nicknameNode = responseNode.get("nickname");
            if (nicknameNode != null) {
                return nicknameNode.asText();
            }
        }
        throw new IllegalArgumentException("Failed to extract nickname from Naver API response");
    }

    private String getNaverEmail(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode responseNode = jsonNode.get("response");
        if (responseNode != null) {
            JsonNode emailNode = responseNode.get("email");
            if (emailNode != null) {
                return emailNode.asText();
            }
        }
        throw new IllegalArgumentException("Failed to extract email from Naver API response");
    }

    private String getNaverUserID(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode responseNode = jsonNode.get("response");
        if (responseNode != null) {
            JsonNode uidNode = responseNode.get("id");
            if (uidNode != null) {
                return uidNode.asText();
            }
        }
        return "Uid Unknown";
    }

    private boolean invalidateAccessToken(String accessToken) {
        String url = "https://nid.naver.com/oauth2.0/token?grant_type=delete" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&access_token=" + accessToken +
                "&service_provider=NAVER";

        try {
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

