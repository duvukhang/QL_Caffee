package com.example.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// IMPORT PHÒNG HỜ: Khang mở comment ra nếu sau này tạo các Service tương ứng
// import com.example.demo.Service.SQL.Staff.SqlStaffService;
// import com.example.demo.Service.Google.SheetService;

@RestController
@RequestMapping("/test")
public class HomeController {

    // Khai báo sẵn cấu trúc Dependency Injection giống hệt bên file .NET cũ của bạn
    // private final SqlStaffService sqlStaffService;
    // private final SheetService sheetService;

    // public HomeController(SqlStaffService sqlStaffService, SheetService sheetService) {
    //     this.sqlStaffService = sqlStaffService;
    //     this.sheetService = sheetService;
    // }

    @GetMapping("/calling")
    public ResponseEntity<?> index() {
        // Sử dụng HashMap phẳng để trả về chuỗi JSON an toàn tuyệt đối
        Map<String, Object> response = new HashMap<>();
        response.put("Message", "Hello from APi");
        
        return ResponseEntity.ok(response);
    }
}
