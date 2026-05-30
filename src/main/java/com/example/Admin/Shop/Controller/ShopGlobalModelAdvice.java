package com.example.Admin.Shop.Controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Service.ShopCurrentUserService;

@ControllerAdvice
public class ShopGlobalModelAdvice {
    private final ShopCurrentUserService currentUserService;

    public ShopGlobalModelAdvice(ShopCurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currentShopUser")
    public ShopUser currentShopUser() {
        return currentUserService.currentUser().orElse(null);
    }
}
