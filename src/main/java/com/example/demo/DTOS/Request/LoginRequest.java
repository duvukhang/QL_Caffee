package com.example.demo.DTOS.Request;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
    private String storeId;
}
