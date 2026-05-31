package com.example.Admin.Shop.Controller;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopUserRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;

@Controller
public class ShopAccountController {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10,11}$");

    private final ShopCurrentUserService currentUserService;
    private final ShopUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopAccountController(ShopCurrentUserService currentUserService, ShopUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/account")
    public String account(Model model) {
        model.addAttribute("accountUser", currentUserService.requireUser());
        return "shop/account";
    }

    @PostMapping("/account")
    public String updateAccount(@RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {
        ShopUser user = currentUserService.requireUser();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();

        if (fullName == null || fullName.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Họ tên không được rỗng");
            return "redirect:/account";
        }
        if (!EMAIL.matcher(normalizedEmail).matches()) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ");
            return "redirect:/account";
        }
        var duplicateEmail = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (duplicateEmail.isPresent() && !duplicateEmail.get().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
            return "redirect:/account";
        }
        if (!normalizedPhone.isBlank()) {
            if (!PHONE.matcher(normalizedPhone).matches()) {
                redirectAttributes.addFlashAttribute("error", "Số điện thoại phải gồm 10-11 chữ số");
                return "redirect:/account";
            }
            var duplicatePhone = userRepository.findFirstByPhoneOrderByIdAsc(normalizedPhone);
            if (duplicatePhone.isPresent() && !duplicatePhone.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại");
                return "redirect:/account";
            }
        }

        user.setEmail(normalizedEmail);
        user.setFullName(fullName.trim());
        user.setPhone(normalizedPhone.isBlank() ? null : normalizedPhone);
        user.setAddress(address == null || address.isBlank() ? null : address.trim());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin cá nhân");
        return "redirect:/account";
    }

    @PostMapping("/account/password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        ShopUser user = currentUserService.requireUser();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/account";
        }
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới tối thiểu 6 ký tự");
            return "redirect:/account";
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu");
        return "redirect:/account";
    }
}
