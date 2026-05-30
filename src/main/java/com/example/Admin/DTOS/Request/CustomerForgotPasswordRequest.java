package com.example.Admin.DTOS.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerForgotPasswordRequest {

    @NotBlank(message = "Vui long nhap email.")
    @Email(message = "Email khong dung dinh dang.")
    @Size(max = 50, message = "Email toi da 50 ky tu.")
    private String email;

    @NotBlank(message = "Vui long nhap so dien thoai.")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "So dien thoai gom 10 so va bat dau bang 0.")
    private String phone;

    @NotBlank(message = "Vui long nhap mat khau moi.")
    @Size(min = 6, max = 50, message = "Mat khau moi phai tu 6 den 50 ky tu.")
    private String newPassword;

    @NotBlank(message = "Vui long xac nhan mat khau moi.")
    private String confirmPassword;
}
