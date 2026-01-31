package com.example.biwooda.auth.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.math.BigInteger;

@Service
public class NaverStateTokenService {

    // Generate a state token to prevent CSRF attacks
    public static String generateState() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    // Store the generated state token in the session
    public static void saveStateToken(HttpSession session) {
        String state = generateState();
        session.setAttribute("state", state);
    }

    public static String getStateToken(HttpSession session) {
        return (String) session.getAttribute("state");
    }
}
