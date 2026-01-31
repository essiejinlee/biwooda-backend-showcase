package com.example.biwooda.auth.model;

public class EmailAuthResponse {
    private final String token;
    private final String message;

    public EmailAuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}