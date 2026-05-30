package com.example.Admin.Shop.Controller;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.ShopRole;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopUserRepository;

@Controller
public class ShopAuthController {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10,11}$");

    private final ShopUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopAuthController(ShopUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "shop/login";
    }

    @GetMapping("/register")
    public String register() {
        return "shop/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {
        String error = validate(username, email, password, phone);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/register";
        }
        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
            redirectAttributes.addFlashAttribute("error", "Username đã tồn tại");
            return "redirect:/register";
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
            return "redirect:/register";
        }

        ShopUser user = new ShopUser();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName == null || fullName.isBlank() ? username.trim() : fullName.trim());
        user.setPhone(phone == null ? null : phone.trim());
        user.setAddress(address == null ? null : address.trim());
        user.setRole(ShopRole.CUSTOMER);
        user.setEnabled(true);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
        return "redirect:/login";
    }

    private String validate(String username, String email, String password, String phone) {
        if (username == null || username.trim().length() < 3) {
            return "Username tối thiểu 3 ký tự";
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            return "Email không hợp lệ";
        }
        if (password == null || password.length() < 6) {
            return "Mật khẩu tối thiểu 6 ký tự";
        }
        if (phone != null && !phone.isBlank() && !PHONE.matcher(phone.trim()).matches()) {
            return "Số điện thoại phải gồm 10-11 chữ số";
        }
        return null;
    }
}
