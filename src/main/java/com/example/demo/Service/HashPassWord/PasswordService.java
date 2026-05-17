package com.example.demo.Service.HashPassWord;

public interface PasswordService {
    String hashPassword(String password);
    boolean verifyPassword(String password, String hashPassword);
}
