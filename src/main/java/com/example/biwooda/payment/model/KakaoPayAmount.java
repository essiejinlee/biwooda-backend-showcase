package com.example.biwooda.payment.model;

import lombok.Getter;

// Response received when sending a payment approval request
@Getter
public class KakaoPayAmount {
    private int total; // Total payment amount
    private int taxFree; // Tax-free amount
    private int tax; // VAT amount

    public int getTotal(){
        return total;
    }
}
