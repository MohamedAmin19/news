package com.news.service;

import com.news.model.LoginRequest;
import com.news.model.LoginResponse;
import com.news.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        if (ADMIN_USERNAME.equals(loginRequest.getUsername()) && 
            ADMIN_PASSWORD.equals(loginRequest.getPassword())) {
            String token = jwtUtil.generateToken(loginRequest.getUsername());
            return new LoginResponse(token, "Login successful");
        }
        return new LoginResponse(null, "Invalid username or password");
    }
}

