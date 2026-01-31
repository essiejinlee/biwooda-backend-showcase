package com.example.biwooda.auth.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoApiService {

    private RestTemplate restTemplate = new RestTemplate();
    private String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

    public String[] getEmailAndName(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        /*
        // Set required parameters for the requests
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(kakaoUserInfoUrl)
                .queryParam("property_keys", "[\"kakao_account.email\",\"kakao_account.name\"]")
                .queryParam("secure_resource", false); // Whether to use HTTPS
        */

        // Configure property keys to retreive from Kakao
        String[] propertyKeys = {"kakao_account.email", "kakao_account.profile.nickname"};
        headers.set("property_keys", String.join(",", propertyKeys));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                kakaoUserInfoUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String email = jsonNode.at("/kakao_account/email").asText();
                String nickname = jsonNode.at("/kakao_account/profile/nickname").asText();
                return new String[]{email, nickname};
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse user info from Kakao response", e);
            }
        } else {
            throw new RuntimeException("Failed to retrieve user info from Kakao");
        }
    }
}


