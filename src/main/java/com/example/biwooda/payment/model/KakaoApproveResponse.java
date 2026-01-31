package com.example.biwooda.payment.model;

import lombok.Getter;
import lombok.Setter;

// Response received when sending an approve request to the KakaoPay API
@Setter
@Getter
public class KakaoApproveResponse {
    private String tid;     // Unique payment transaction ID
    private String sid;    // Unique ID for recurring payments
    private KakaoPayAmount amount; // Payment amount
    private String item_name; // Item name
    private String created_at; // Payment request timestamp
    private String approved_at; // Payment approval timestamp

    public String getTid(){ return tid; }
    public String getSid() { return sid; }

    public KakaoPayAmount getAmount(){
        return amount;
    }
    public String getCreated_at(){
        return created_at;
    }

    public String getApproved_at(){
        return approved_at;
    }

    public String getItem_name(){
        return item_name;
    }
}
