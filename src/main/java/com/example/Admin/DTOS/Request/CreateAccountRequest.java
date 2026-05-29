package com.example.Admin.DTOS.Request;

import lombok.Data;

@Data
public class CreateAccountRequest {
    private String accountId;
    private String userName;
    private String password;
    private String roleId;
}