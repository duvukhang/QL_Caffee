package com.example.demo.Controller.Manager;

import com.example.demo.Models.Staff;
import com.example.demo.Repositories.StaffRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/Staff")
@PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
public class StaffManagerController {

    private final StaffRepository staffRepository;

    public StaffManagerController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllStaffSimple() {
        List<Staff> staffs = staffRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Staff s : staffs) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", s.getStaffId());
            map.put("fullName", s.getStaffName());
            map.put("roleName", s.getRole() != null ? s.getRole().getRoleName() : "Manager");
            map.put("storeId", "HUIT");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createJson")
    public ResponseEntity<?> createStaffJson(@RequestBody Map<String, String> body) {
        Staff staff = new Staff();
        staff.setStaffId("ST" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        staff.setStaffName(body.get("fullName"));
        staff.setStatus("Hoạt động");
        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Thêm nhân viên thành công"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateStaffJson(@PathVariable String id, @RequestBody Map<String, String> body) {
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        staff.setStaffName(body.get("fullName"));
        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStaffSimple(@PathVariable String id) {
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        staff.setStatus("Nghỉ việc");
        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }
}