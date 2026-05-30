package com.example.Admin.MidWare.JWT;

import com.example.Admin.DTOS.Request.LoginRequest;
import com.example.Admin.Models.Customer;
import com.example.Admin.Models.Staff;
import com.example.Admin.Models.Sysrole;
import com.example.Admin.Models.Sysuser;
import com.example.Admin.Repositories.SysUserRepository;
import com.example.Admin.Service.Customer.CustomerAccountService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;
    private final SysUserRepository sysUserRepository;
    private final PasswordService passwordService;
    private final CustomerAccountService customerAccountService;

    public AuthController(
            TokenService tokenService,
            SysUserRepository sysUserRepository,
            PasswordService passwordService,
            CustomerAccountService customerAccountService
    ) {
        this.tokenService = tokenService;
        this.sysUserRepository = sysUserRepository;
        this.passwordService = passwordService;
        this.customerAccountService = customerAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request == null) {
                throw new RuntimeException("Request body is empty.");
            }
            String username = request.getUserName() != null ? request.getUserName().trim() : "";
            logger.info("Login request for account: {}", username);

            Optional<Sysuser> staffAccount = sysUserRepository.findByUserNameIgnoreCase(username);
            if (staffAccount.isPresent()) {
                return ResponseEntity.ok(loginStaff(staffAccount.get(), request.getPassword()));
            }

            Customer customer = customerAccountService.login(username, request.getPassword())
                    .orElseThrow(() -> new RuntimeException("Tai khoan hoac mat khau khong chinh xac."));

            return ResponseEntity.ok(loginCustomer(customer));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    private Map<String, Object> loginStaff(Sysuser user, String password) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Du lieu mat khau bi hong.");
        }
        if (!passwordService.verifyPassword(password, user.getPassword())) {
            throw new RuntimeException("Mat khau khong chinh xac.");
        }

        Sysrole role = user.getRole();
        if (role == null || role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw new RuntimeException("Tai khoan chua duoc phan quyen.");
        }

        TokenService.TokenPair pair = tokenService.createTokenPair(user.getUserId(), role.getRoleName());
        Staff staff = user.getStaff();

        Map<String, Object> response = new HashMap<>();
        response.put("token", pair.getAccessToken());
        response.put("userName", user.getUserName());
        response.put("roles", role.getRoleName());
        response.put("accountType", "Staff");
        response.put("redirectUrl", "/index.html");
        response.put("staffName", staff != null ? staff.getStaffName() : "Quan Tri Vien");
        response.put("avatar", staff != null ? staff.getAvatar() : "default.png");
        return response;
    }

    private Map<String, Object> loginCustomer(Customer customer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountType", "Customer");
        claims.put("userName", customer.getUserName());

        TokenService.TokenPair pair = tokenService.createTokenPair(
                customer.getCustomerId(),
                "Customer",
                claims
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", pair.getAccessToken());
        response.put("userName", customer.getUserName());
        response.put("roles", "Customer");
        response.put("accountType", "Customer");
        response.put("customerId", customer.getCustomerId());
        response.put("redirectUrl", "/");
        return response;
    }
}
