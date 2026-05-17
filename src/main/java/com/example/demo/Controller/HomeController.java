package com.example.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class HomeController {

    @GetMapping("/calling")
    public ResponseEntity<?> index() {
        // Sử dụng HashMap phẳng để trả về chuỗi JSON an toàn tuyệt đối
        Map<String, Object> response = new HashMap<>();
        response.put("Message", "Hello from APi");
        
        return ResponseEntity.ok(response);
    }
}