package com.example.Admin.Controller.Manager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Admin.Models.Staff;
import com.example.Admin.Models.Store;
import com.example.Admin.Models.Sysrole;
import com.example.Admin.Repositories.StaffRepository;
import com.example.Admin.Repositories.StoreRepository;
import com.example.Admin.Repositories.SysRoleRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/Staff")
@PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
public class StaffManagerController {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;
    private final SysRoleRepository sysRoleRepository;

    public StaffManagerController(StaffRepository staffRepository, StoreRepository storeRepository, SysRoleRepository sysRoleRepository) {
        this.staffRepository = staffRepository;
        this.storeRepository = storeRepository;
        this.sysRoleRepository = sysRoleRepository;
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
            map.put("storeId", s.getStore() != null ? s.getStore().getStoreId() : "HUIT");
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
        staff.setAvatar("default.png"); // Set mặc định để tránh null DB
        
        // Cập nhật Role và Store
        Sysrole role = sysRoleRepository.findById(body.get("roleName")).orElse(null);
        if (role != null) staff.setRole(role);
        
        Store store = storeRepository.findById(body.get("storeId")).orElse(null);
        if (store != null) staff.setStore(store);

        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Thêm nhân viên thành công"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateStaffJson(@PathVariable String id, @RequestBody Map<String, String> body) {
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        staff.setStaffName(body.get("fullName"));
        
        Sysrole role = sysRoleRepository.findById(body.get("roleName")).orElse(null);
        if (role != null) staff.setRole(role);
        
        Store store = storeRepository.findById(body.get("storeId")).orElse(null);
        if (store != null) staff.setStore(store);

        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStaffSimple(@PathVariable String id) {
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        staff.setStatus("Nghỉ việc");
        staffRepository.save(staff);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }
}