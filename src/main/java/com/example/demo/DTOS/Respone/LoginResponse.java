package com.example.demo.DTOS.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Tự động tạo Constructor chứa toàn bộ tham số giống hệt C#
public class LoginResponse {
    private String accessToken;
    private String userName;
    private String fullName;
    private String role;
    private String avatar;
}
