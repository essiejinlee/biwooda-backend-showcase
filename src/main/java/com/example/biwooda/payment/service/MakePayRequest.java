package com.example.biwooda.payment.service;

import com.example.biwooda.payment.model.KakaoPayItemInfo;
import com.example.biwooda.payment.model.KakaoPayRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
@RequiredArgsConstructor
public class MakePayRequest {
    static final String cid = "TCSUBSCRIP"; // 가맹점 테스트 코드

    //결제 준비 요청 데이터 생성 메소드
    public KakaoPayRequest getReadyRequest(String uid, KakaoPayItemInfo itemInfo){
        String orderId="point"+uid;
        int num = itemInfo.getNum();
        int price = itemInfo.getPrice();
        int total_amount = num * price;

        //카카오페이 요청 양식
        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", cid);
        parameters.add("partner_order_id",orderId); //가맹점 주문번호
        parameters.add("partner_user_id", uid);  //가맹점 회원 id
        parameters.add("item_name", itemInfo.getItemName());  //상품명
        parameters.add("quantity", num+"");  //상폼 수량
        parameters.add("total_amount", total_amount+"");  //상품 총액
        //parameters.add("vat_amount", "부가세");   //부가세
        parameters.add("tax_free_amount", "0");  //상품 비과세 금액
        parameters.add("approval_url", "http://localhost:4000/payment/success"+"/"+uid); // 성공 시 redirect url
        parameters.add("cancel_url", "http://localhost:4000/payment/cancel"+"/"+uid); // 취소 시 redirect url
        parameters.add("fail_url", "http://localhost:4000/payment/fail"+"/"+uid); // 실패 시 redirect url

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/ready", parameters);

    }

    //결제 승인 요청 생성 메소드
    public KakaoPayRequest getApproveRequest(String uid, String tid, String pgToken){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();

        String orderId="point"+uid;
        // 가맹점 코드 테스트코드는 TC0ONETIME 이다.
        map.add("cid", "TCSUBSCRIP");

        // getReadyRequest 에서 받아온 tid
        map.add("tid", tid);
        map.add("partner_order_id", orderId); // 주문명
        map.add("partner_user_id", uid);

        // getReadyRequest에서 받아온 redirect url에 클라이언트가
        // 접속하여 결제를 성공시키면 아래의 url로 redirect 되는데
        //http://localhost:4000/payment/success"+"/"+id
        // 여기에 &pg_token= 토큰값 이 붙어서 redirect 된다.
        // 해당 내용을 뽑아 내서 사용하면 된다.
        map.add("pg_token", pgToken);

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/approve",map);
    }

}
