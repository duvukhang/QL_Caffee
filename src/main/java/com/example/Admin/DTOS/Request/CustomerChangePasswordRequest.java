package com.example.Admin.DTOS.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerChangePasswordRequest {

    @NotBlank(message = "Vui long nhap mat khau hien tai.")
    private String oldPassword;

    @NotBlank(message = "Vui long nhap mat khau moi.")
    @Size(min = 6, max = 50, message = "Mat khau moi phai tu 6 den 50 ky tu.")
    private String newPassword;

    @NotBlank(message = "Vui long xac nhan mat khau moi.")
    private String confirmPassword;
}
