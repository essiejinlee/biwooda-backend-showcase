package com.example.biwooda.payment.model;

import lombok.Getter;

// Item information received from a client
@Getter
public class KakaoPayItemInfo {
    private int price;
    private String itemName;
    private int num;

    public int getNum() {
        return num;
    }
    public int getPrice() {
        return price;
    }
    public String getItemName(){
        return itemName;
    }
}
