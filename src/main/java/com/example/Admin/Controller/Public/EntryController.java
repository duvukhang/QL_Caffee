package com.example.Admin.Controller.Public;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EntryController {

    @GetMapping("/admin")
    public String admin(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STAFF".equals(authority.getAuthority()))) {
            return "redirect:/staff/pos";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/customer")
    public String customer() {
        return "redirect:/";
    }
}
