package com.example.Admin.DTOS.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerProfileRequest {

    @NotBlank(message = "Vui long nhap ho ten.")
    @Size(max = 50, message = "Ho ten toi da 50 ky tu.")
    private String fullName;

    @NotBlank(message = "Vui long nhap so dien thoai.")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "So dien thoai gom 10 so va bat dau bang 0.")
    private String phone;

    @Size(max = 100, message = "Dia chi toi da 100 ky tu.")
    private String address;

    @NotBlank(message = "Vui long nhap email.")
    @Email(message = "Email khong dung dinh dang.")
    @Size(max = 50, message = "Email toi da 50 ky tu.")
    private String email;

    @Pattern(regexp = "^$|^[0-9]{9,11}$", message = "CMND/CCCD gom 9 den 11 so.")
    private String idNumber;

    private String gender;
}
