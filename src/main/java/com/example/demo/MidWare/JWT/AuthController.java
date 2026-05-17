package com.example.demo.MidWare.JWT;

import com.example.demo.DTOS.Request.LoginRequest;
import com.example.demo.MidWare.Filter.CustomError;
import com.example.demo.Models.Sysrole;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.SysUserRepository; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;
    private final SysUserRepository sysUserRepository;
    private final PasswordService passwordService;

    public AuthController(TokenService tokenService, SysUserRepository sysUserRepository, PasswordService passwordService) {
        this.tokenService = tokenService;
        this.sysUserRepository = sysUserRepository;
        this.passwordService = passwordService;
    }

    private Map<String, Object> validateUser(String username, String password) {
        // Tìm user dựa trên userName
        Sysuser user = sysUserRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User Not Exists"));

        // Kiểm tra trạng thái cửa hàng
        if (user.getStore() != null && "Ngưng hoạt động".equals(user.getStore().getStoreStatus())) {
            throw new RuntimeException("Store is inactive");
        }
        if (user.getStaff() == null) {
            throw new RuntimeException("User doesn't map with any Staff");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new CustomError(422, "Unprocessable Entity", "Your KeyWord not true");
        }

        // Kiểm tra so khớp mật khẩu
        if (passwordService.verifyPassword(password, user.getPassword())) {
            
            // ĐÃ FIX LỖI getSysrole(): Tự động nhận diện linh hoạt theo Model thực tế của bạn
            Sysrole role = null;
            try {
                // Cách 1: Thử gọi hàm getSysRole() theo chuẩn camelCase của Java
                java.lang.reflect.Method method = user.getClass().getMethod("getSysRole");
                role = (Sysrole) method.invoke(user);
            } catch (Exception e1) {
                try {
                    // Cách 2: Thử gọi hàm getSysrole() viết thường hoàn toàn theo C# cũ
                    java.lang.reflect.Method method = user.getClass().getMethod("getSysrole");
                    role = (Sysrole) method.invoke(user);
                } catch (Exception e2) {
                    try {
                        // Cách 3: Thử gọi hàm ngắn gọn getRole()
                        java.lang.reflect.Method method = user.getClass().getMethod("getRole");
                        role = (Sysrole) method.invoke(user);
                    } catch (Exception e3) {
                        throw new RuntimeException("Không tìm thấy hàm getter cho Sysrole trong Model Sysuser.java");
                    }
                }
            }

            if (role == null) {
                throw new RuntimeException("Can't be Authentication");
            }

            Map<String, Object> authResult = new HashMap<>();
            authResult.put("user", user.getStaff());
            authResult.put("userId", user.getUserId());
            authResult.put("roleName", role.getRoleName());
            return authResult;
        } else {
            throw new RuntimeException("Can't be Authentication");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("API Login is being called:");

        // Tái hiện logic C#: Tự động nối chuỗi StoreId nếu không phải là admin
        if (!"admin".equals(request.getUserName())) {
            request.setUserName(request.getStoreId() + "_" + request.getUserName());
        }

        try {
            Map<String, Object> authResult = validateUser(request.getUserName(), request.getPassword());
            
            var staff = (com.example.demo.Models.Staff) authResult.get("user");
            int userId = (int) authResult.get("userId");
            String roleName = (String) authResult.get("roleName");

            // Tạo chuỗi Token cặp
            TokenService.TokenPair pair = tokenService.createTokenPair(userId, roleName);

            // Trả về cấu hình JSON Response giống bản cũ của bạn
            Map<String, Object> loginResponse = new HashMap<>();
            loginResponse.put("accessToken", pair.getAccessToken());
            loginResponse.put("userName", request.getUserName());
            loginResponse.put("staffName", staff.getStaffName());
            loginResponse.put("roles", roleName);
            loginResponse.put("avatar", staff.getAvatar());

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            throw new org.springframework.security.authentication.BadCredentialsException("User Infor not true");
        }
    }
}