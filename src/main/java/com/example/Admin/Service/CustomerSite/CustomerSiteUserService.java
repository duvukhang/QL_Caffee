package com.example.Admin.Service.CustomerSite;

import com.example.Admin.DTOS.Customer.EditProfileForm;
import com.example.Admin.DTOS.Customer.RegisterForm;
import com.example.Admin.DTOS.Request.CustomerForgotPasswordRequest;
import com.example.Admin.DTOS.Request.CustomerProfileRequest;
import com.example.Admin.DTOS.Request.CustomerRegisterRequest;
import com.example.Admin.Models.Customer;
import com.example.Admin.Models.CustomerDetail;
import com.example.Admin.Service.Customer.CustomerAccountService;
import org.springframework.stereotype.Service;

@Service
public class CustomerSiteUserService {

    private final CustomerAccountService customerAccountService;

    public CustomerSiteUserService(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

    public Customer login(String username, String rawPassword) {
        return customerAccountService.login(username, rawPassword).orElse(null);
    }

    public boolean passwordMatches(String rawPassword, String storedPassword) {
        return customerAccountService.passwordMatches(rawPassword, storedPassword);
    }

    public String register(RegisterForm form) {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail(form.getEmail());
        request.setPassword(form.getPassword());
        request.setFullName(form.getFullname());
        request.setAddress(form.getAddress());
        request.setPhone(form.getPhone());
        request.setIdNumber(form.getCccd());
        request.setGender(form.getGender());
        return customerAccountService.register(request);
    }

    public CustomerDetail updateProfile(String customerId, EditProfileForm form) {
        CustomerProfileRequest request = new CustomerProfileRequest();
        request.setEmail(form.getEmail());
        request.setFullName(form.getFullName());
        request.setPhone(form.getPhone());
        request.setAddress(form.getAddress());
        request.setIdNumber(form.getIdNumber());
        request.setGender(form.getGender());
        return customerAccountService.updateProfile(customerId, request);
    }

    public void resetPassword(CustomerForgotPasswordRequest request) {
        customerAccountService.resetPassword(request);
    }
}
