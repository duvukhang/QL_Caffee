package com.example.demo.MidWare.JWT;

import com.example.demo.DTOS.Request.LoginRequest;
import com.example.demo.Models.Sysrole;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.SysUserRepository; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
// 🛠️ ĐÃ FIX: Đồng bộ URL cho khớp với Javascript và SecurityConfig
@RequestMapping("/api/auth") 
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
        Sysuser user = sysUserRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại trong hệ thống."));

        // 🛠️ ĐÃ BỎ: Kiểm tra trạng thái Store inactive vì không còn dùng chi nhánh
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Dữ liệu mật khẩu bị hỏng.");
        }

        if (passwordService.verifyPassword(password, user.getPassword())) {
            
            Sysrole role = null;
            try {
                java.lang.reflect.Method method = user.getClass().getMethod("getSysRole");
                role = (Sysrole) method.invoke(user);
            } catch (Exception e1) {
                try {
                    java.lang.reflect.Method method = user.getClass().getMethod("getSysrole");
                    role = (Sysrole) method.invoke(user);
                } catch (Exception e2) {
                    try {
                        java.lang.reflect.Method method = user.getClass().getMethod("getRole");
                        role = (Sysrole) method.invoke(user);
                    } catch (Exception e3) {
                        throw new RuntimeException("Hệ thống lỗi: Tài khoản chưa được phân quyền.");
                    }
                }
            }

            if (role == null) {
                throw new RuntimeException("Hệ thống lỗi: Dữ liệu phân quyền bị trống.");
            }

            Map<String, Object> authResult = new HashMap<>();
            authResult.put("user", user.getStaff()); // Lưu ý: Admin có thể không có Staff (trả về null)
            authResult.put("userId", user.getUserId());
            authResult.put("roleName", role.getRoleName());
            return authResult;
        } else {
            throw new RuntimeException("Mật khẩu không chính xác.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Yêu cầu đăng nhập từ tài khoản: " + request.getUserName());

        try {
            // 🛠️ ĐÃ BỎ: Logic cộng chuỗi request.getStoreId() + "_" + request.getUserName()
            // Bây giờ hệ thống sẽ đọc thẳng username mộc (VD: "admin")
            
            Map<String, Object> authResult = validateUser(request.getUserName(), request.getPassword());
            
            var staff = (com.example.demo.Models.Staff) authResult.get("user");
            int userId = (int) authResult.get("userId");
            String roleName = (String) authResult.get("roleName");

            TokenService.TokenPair pair = tokenService.createTokenPair(userId, roleName);

            Map<String, Object> loginResponse = new HashMap<>();
            // 🛠️ QUAN TRỌNG: Trả về field 'token' cho khớp với file JS (data.token)
            loginResponse.put("token", pair.getAccessToken()); 
            loginResponse.put("userName", request.getUserName());
            loginResponse.put("roles", roleName);
            
            // Xử lý an toàn nếu tài khoản Admin không được gán vào 1 nhân viên cụ thể
            if (staff != null) {
                loginResponse.put("staffName", staff.getStaffName());
                loginResponse.put("avatar", staff.getAvatar());
            } else {
                loginResponse.put("staffName", "Quản Trị Viên");
                loginResponse.put("avatar", "default.png");
            }

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            // 🛠️ ĐÃ FIX: Trả về HTTP 401 Unauthorized kèm thông báo lỗi JSON
            // Frontend Javascript sẽ hứng mã này và hiển thị lỗi màu đỏ lên màn hình mượt mà
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}