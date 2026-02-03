package com.example.biwooda.payment.service;

import com.example.biwooda.payment.model.KakaoPayItemInfo;
import com.example.biwooda.payment.model.KakaoPayRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
@RequiredArgsConstructor
public class MakePayRequest {
    static final String cid = "TCSUBSCRIP"; // Merchant test code

    // Create request data for payment initialisation
    public KakaoPayRequest getReadyRequest(String uid, KakaoPayItemInfo itemInfo){
        String orderId="point"+uid;
        int num = itemInfo.getNum();
        int price = itemInfo.getPrice();
        int total_amount = num * price;

        // Build KakaoPay request parameters
        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", cid);
        parameters.add("partner_order_id",orderId); // Merchant order ID
        parameters.add("partner_user_id", uid);  // Merchant user ID
        parameters.add("item_name", itemInfo.getItemName());  // Item name
        parameters.add("quantity", num+"");  // Item quantity
        parameters.add("total_amount", total_amount+"");  // Total amount
        parameters.add("vat_amount", "부가세");   // VAT amount
        parameters.add("tax_free_amount", "0");  // Tax-free amount
        parameters.add("approval_url", "http://localhost:4000/payment/success"+"/"+uid); // Redirect URL on payment success
        parameters.add("cancel_url", "http://localhost:4000/payment/cancel"+"/"+uid); // Redirect URL on payment cancellation
        parameters.add("fail_url", "http://localhost:4000/payment/fail"+"/"+uid); // Redirect URL on payment failure

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/ready", parameters);

    }

    // Create request data for payment approval
    public KakaoPayRequest getApproveRequest(String uid, String tid, String pgToken){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();

        String orderId="point"+uid;
        // Merchant test code (TC0ONETIME for one-time payments)
        map.add("cid", "TCSUBSCRIP");

        // tid received from the ready request
        map.add("tid", tid);
        map.add("partner_order_id", orderId);
        map.add("partner_user_id", uid);

        /*
         * After completing the payment on the client side,
         * KakaoPay redirects the user to the approval URL:
         *   http://localhost:4000/payment/success/{uid}
         *
         * The redirect URL contains the pg_token parameter,
         * which is extracted and used for payment approval.
         */
        map.add("pg_token", pgToken);

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/approve",map);
    }

}
