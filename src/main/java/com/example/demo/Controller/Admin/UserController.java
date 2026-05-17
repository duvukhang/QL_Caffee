package com.example.demo.Controller.Admin;

// Import đầy đủ các class của Spring Web
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Import Spring Security
import org.springframework.security.access.prepost.PreAuthorize;

// Import Map của Java
import java.util.Map;

// Import Service của dự án (Lưu ý đường dẫn package nếu bạn đã đổi tên theo chuẩn mới)
import com.example.demo.Service.SQL.Staff.SqlStaffService;

@RestController
@RequestMapping("/Admin/User")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SqlStaffService sqlStaffService;

    public UserController(SqlStaffService sqlStaffService) {
        this.sqlStaffService = sqlStaffService;
    }

    @PutMapping("/StoreAccount/{userName}")
    public ResponseEntity<?> assignUserToStaff(@RequestParam("StaffId") String staffId, @PathVariable("userName") String userName) {
        String message = sqlStaffService.assignUserToStaff(staffId, userName);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/StoreAccount")
    public ResponseEntity<?> storeAccount(@RequestParam("StoreId") String storeId, @RequestParam("RoleId") String roleId) {
        if ("1".equals(roleId)) roleId = "";
        if ("1".equals(storeId)) storeId = "";
        var response = sqlStaffService.getStoreAccountsAsync(storeId, roleId);
        if (response != null) return ResponseEntity.ok(response);
        throw new RuntimeException("Can't get account from database");
    }

    @GetMapping("/StoreAccountPage")
    public ResponseEntity<?> storeAccountPage(@RequestParam("PageNum") int pageNum, @RequestParam("PageSize") int pageSize) {
        if (pageSize > 100 || pageSize <= 0) throw new IllegalArgumentException("PageSize must be between 1 and 100");
        var response = sqlStaffService.getPageAccountAsync(pageNum, pageSize);
        if (response != null) return ResponseEntity.ok(response);
        throw new RuntimeException("Can't get data from database");
    }
}