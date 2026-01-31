package com.example.biwooda.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;

@Getter
// Request to be sent to the KakaoPay server
public class KakaoPayRequest {
    private String url;   // KakaoPay API URL
    private LinkedMultiValueMap<String,String> parameters;   // Parameters to be included in the request

    public KakaoPayRequest(String url, LinkedMultiValueMap<String, String> parameters) {
        this.url = url;
        this.parameters = parameters;
    }

    public String getUrl(){
        return url;
    }

    public LinkedMultiValueMap<String,String> getParameters(){
        return parameters;
    }
}
