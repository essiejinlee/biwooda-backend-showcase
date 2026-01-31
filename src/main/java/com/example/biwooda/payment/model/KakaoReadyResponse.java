package com.example.biwooda.payment.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// Data received when sending a request to the KakaoPay API
@Getter
@Setter
@ToString
public class KakaoReadyResponse {
    private String tid;   // Unique payment transaction ID
    private String next_redirect_mobile_url; // Payment page URL for mobile web
    private String next_redirect_pc_url;     // Payment page URL for PC web
    private String created_at;               // Payment request timestamp

    public String getTid(){
        return tid;
    }

    public String getNext_redirect_mobile_url(){
        return next_redirect_mobile_url;
    }

    public String getNext_redirect_pc_url(){
        return next_redirect_pc_url;
    }

    public String getCreated_at(){
        return created_at;
    }
}
