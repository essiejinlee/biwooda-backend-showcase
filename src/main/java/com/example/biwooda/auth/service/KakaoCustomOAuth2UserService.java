package com.example.biwooda.auth.service;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class KakaoCustomOAuth2UserService extends DefaultOAuth2UserService {

    private final FirebaseCustomService firebaseCustomService;

    public KakaoCustomOAuth2UserService(FirebaseCustomService firebaseCustomService) {
        this.firebaseCustomService = firebaseCustomService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Retrieve Kakao user ID
        String kakaoUserId = String.valueOf(attributes.get("id"));

        // Generate Firebase custom token
        String firebaseToken = "";
        try {
            if (StringUtils.hasText(kakaoUserId)) {
                firebaseToken = firebaseCustomService.createFirebaseToken(kakaoUserId);
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to create Firebase token", e);
        }

        // Add Firebase token to user attributes
        attributes.put("firebaseToken", firebaseToken);

        // Create a new OAuth2User using DefaultOAuth2User
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
    }
}

