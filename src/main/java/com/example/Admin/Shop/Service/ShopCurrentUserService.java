package com.example.Admin.Shop.Service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopUserRepository;

@Service
public class ShopCurrentUserService {
    private final ShopUserRepository userRepository;

    public ShopCurrentUserService(ShopUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<ShopUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(authentication.getName());
    }

    public ShopUser requireUser() {
        return currentUser().orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập"));
    }
}
