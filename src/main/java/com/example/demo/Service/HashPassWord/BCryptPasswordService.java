package com.example.demo.Service.HashPassWord;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordService implements PasswordService {
    
    private final BCryptPasswordEncoder passwordEncoder;

    public BCryptPasswordService() {
        // Độ mạnh workFactor tương đương mặc định là 10
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verifyPassword(String password, String hashPassword) {
        return passwordEncoder.matches(password, hashPassword);
    }
}
