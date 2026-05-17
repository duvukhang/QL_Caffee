package com.example.demo.DTOS.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginRequest {
    
    @JsonProperty("username") // ĐÃ FIX: Ép hệ thống nhận key 'username' từ JS
    private String userName;
    
    @JsonProperty("password")
    private String password;
    
    // ĐÃ XÓA: storeId vì hệ thống đã quản lý tập trung
}