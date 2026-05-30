package com.example.Admin.Controller.Public;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EntryController {

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/login.html";
    }

    @GetMapping("/customer")
    public String customer() {
        return "redirect:/";
    }
}
