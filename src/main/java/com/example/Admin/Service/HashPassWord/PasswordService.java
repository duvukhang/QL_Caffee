package com.example.Admin.Service.HashPassWord;

public interface PasswordService {
    String hashPassword(String password);
    boolean verifyPassword(String password, String hashPassword);
}
