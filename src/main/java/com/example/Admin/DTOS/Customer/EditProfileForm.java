package com.example.Admin.DTOS.Customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EditProfileForm {

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp h\u1ecd t\u00ean")
    @Size(max = 50, message = "H\u1ecd t\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c qu\u00e1 50 k\u00fd t\u1ef1")
    private String fullName;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i")
    @Pattern(regexp = "^0[0-9]{9}$", message = "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i ph\u1ea3i g\u1ed3m 10 ch\u1eef s\u1ed1 v\u00e0 b\u1eaft \u0111\u1ea7u b\u1eb1ng 0")
    private String phone;

    @Size(max = 100, message = "\u0110\u1ecba ch\u1ec9 kh\u00f4ng \u0111\u01b0\u1ee3c qu\u00e1 100 k\u00fd t\u1ef1")
    private String address;

    @Email(message = "Email kh\u00f4ng \u0111\u00fang \u0111\u1ecbnh d\u1ea1ng")
    @Size(max = 50, message = "Email kh\u00f4ng \u0111\u01b0\u1ee3c qu\u00e1 50 k\u00fd t\u1ef1")
    private String email;

    @Pattern(regexp = "^$|^[0-9]{9,11}$", message = "CMND/CCCD ph\u1ea3i g\u1ed3m 9 \u0111\u1ebfn 11 s\u1ed1")
    private String idNumber;

    @Pattern(regexp = "^$|Nam|N\u1eef|Kh\u00e1c", message = "Gi\u1edbi t\u00ednh kh\u00f4ng h\u1ee3p l\u1ec7")
    private String gender;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = trim(fullName);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = digits(phone);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = trim(address);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trim(email);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = digits(idNumber);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = trim(gender);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String digits(String value) {
        return value == null ? null : value.trim().replaceAll("\\D", "");
    }
}
