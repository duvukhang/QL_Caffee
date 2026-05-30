package com.example.Admin.DTOS.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordForm {

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp m\u1eadt kh\u1ea9u hi\u1ec7n t\u1ea1i")
    private String oldPassword;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp m\u1eadt kh\u1ea9u m\u1edbi")
    @Size(min = 6, max = 50, message = "M\u1eadt kh\u1ea9u m\u1edbi ph\u1ea3i t\u1eeb 6 \u0111\u1ebfn 50 k\u00fd t\u1ef1")
    private String newPassword;

    private String confirmPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
