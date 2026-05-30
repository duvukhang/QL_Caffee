package com.example.Admin.Controller.CustomerSite;

import com.example.Admin.DTOS.Customer.ChangePasswordForm;
import com.example.Admin.DTOS.Customer.EditProfileForm;
import com.example.Admin.DTOS.Customer.RegisterForm;
import com.example.Admin.DTOS.Request.CustomerForgotPasswordRequest;
import com.example.Admin.Models.Customer;
import com.example.Admin.Models.CustomerDetail;
import com.example.Admin.Repositories.CustomerDetailRepository;
import com.example.Admin.Repositories.CustomerRepository;
import com.example.Admin.Service.CustomerSite.CustomerSiteUserService;
import com.example.Admin.Util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class CustomerUserPageController {

    private final CustomerSiteUserService userService;
    private final CustomerRepository customerRepository;
    private final CustomerDetailRepository detailRepository;
    private final PasswordEncoder encoder;

    public CustomerUserPageController(
            CustomerSiteUserService userService,
            CustomerRepository customerRepository,
            CustomerDetailRepository detailRepository,
            PasswordEncoder encoder
    ) {
        this.userService = userService;
        this.customerRepository = customerRepository;
        this.detailRepository = detailRepository;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam("userName") String userName,
            @RequestParam("password") String password,
            @RequestParam(value = "rememberMe", required = false) String rememberMe,
            HttpSession session,
            Model model
    ) {
        Customer customer = userService.login(userName, password);
        if (customer != null) {
            SessionHelper.login(session, customer);
            if (rememberMe != null) {
                session.setMaxInactiveInterval(60 * 60 * 24 * 7);
            }
            return "redirect:/";
        }

        model.addAttribute("error", "T\u00ean \u0111\u0103ng nh\u1eadp ho\u1eb7c m\u1eadt kh\u1ea9u kh\u00f4ng ch\u00ednh x\u00e1c");
        return "user/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "user/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            return "user/register";
        }

        try {
            String customerId = userService.register(registerForm);
            redirectAttributes.addFlashAttribute(
                    "registerSuccess",
                    "\u0110\u0103ng k\u00fd th\u00e0nh c\u00f4ng! ID c\u1ee7a b\u1ea1n l\u00e0: " + customerId
            );
            return "redirect:/user/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        if (!model.containsAttribute("forgotPasswordRequest")) {
            model.addAttribute("forgotPasswordRequest", new CustomerForgotPasswordRequest());
        }
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @Valid @ModelAttribute("forgotPasswordRequest") CustomerForgotPasswordRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            return "user/forgot-password";
        }

        try {
            userService.resetPassword(request);
            redirectAttributes.addFlashAttribute("registerSuccess", "\u0110\u1ed5i m\u1eadt kh\u1ea9u th\u00e0nh c\u00f4ng. Vui l\u00f2ng \u0111\u0103ng nh\u1eadp l\u1ea1i.");
            return "redirect:/user/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user/forgot-password";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        CustomerDetail detail = detailRepository.findByEmailOrCustomerId(customerId, customerId).orElse(null);
        if (detail == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", detail);
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfile(HttpSession session, Model model) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        CustomerDetail detail = detailRepository.findById(customerId).orElse(null);
        if (detail == null) {
            return "error/404";
        }

        EditProfileForm form = new EditProfileForm();
        form.setFullName(detail.getFullName());
        form.setAddress(detail.getAddr());
        form.setPhone(detail.getPhoneNum());
        form.setEmail(detail.getEmail());
        form.setIdNumber(detail.getIdNumber());
        form.setGender(detail.getGender());
        model.addAttribute("editProfileForm", form);
        return "user/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfileSubmit(
            @Valid @ModelAttribute("editProfileForm") EditProfileForm editProfileForm,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            return "user/edit-profile";
        }

        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        try {
            userService.updateProfile(customerId, editProfileForm);
            customerRepository.findById(customerId).ifPresent(customer -> SessionHelper.login(session, customer));
            redirectAttributes.addFlashAttribute("successMessage", "C\u1eadp nh\u1eadt h\u1ed3 s\u01a1 th\u00e0nh c\u00f4ng!");
            return "redirect:/user/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user/edit-profile";
        }
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, HttpSession session) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "user/change-password";
        }
        if (!changePasswordForm.getNewPassword().equals(changePasswordForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "notMatch", "M\u1eadt kh\u1ea9u x\u00e1c nh\u1eadn kh\u00f4ng kh\u1edbp.");
            return "user/change-password";
        }

        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return "redirect:/user/login";
        }
        if (!userService.passwordMatches(changePasswordForm.getOldPassword(), customer.getPassword())) {
            result.rejectValue("oldPassword", "wrong", "M\u1eadt kh\u1ea9u hi\u1ec7n t\u1ea1i kh\u00f4ng ch\u00ednh x\u00e1c.");
            return "user/change-password";
        }

        customer.setPassword(encoder.encode(changePasswordForm.getNewPassword()));
        customerRepository.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "\u0110\u1ed5i m\u1eadt kh\u1ea9u th\u00e0nh c\u00f4ng!");
        return "redirect:/user/profile";
    }
}
