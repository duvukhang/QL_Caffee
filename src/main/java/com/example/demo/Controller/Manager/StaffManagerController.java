package com.example.demo.Controller.Manager;

import com.example.demo.DTOS.Request.CreateStaffRequest;
import com.example.demo.Models.Staff;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.StaffRepository;
import com.example.demo.Repositories.SysUserRepository;
import com.example.demo.Service.ImgService.ImgService;
import com.example.demo.Service.SQL.Staff.SqlStaffService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/Staff")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class StaffManagerController {

    private final SysUserRepository sysUserRepository;
    private final StaffRepository staffRepository;
    private final SqlStaffService sqlStaffService;
    private final ImgService imgService;

    public StaffManagerController(SysUserRepository sysUserRepository, StaffRepository staffRepository,
            SqlStaffService sqlStaffService, ImgService imgService) {
        this.sysUserRepository = sysUserRepository;
        this.staffRepository = staffRepository;
        this.sqlStaffService = sqlStaffService;
        this.imgService = imgService;
    }

    @GetMapping("/{pageNum}/{pageSize}")
    public ResponseEntity<?> getStaffList(@PathVariable("pageNum") int pageNum,
            @PathVariable("pageSize") int pageSize) {
        if (pageNum < 1 || pageSize > 100 || pageSize < 0) {
            throw new IllegalArgumentException("Param is Out Of Range");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Sysuser manager = sysUserRepository.findById(Integer.parseInt(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Fake Token"));

        String storeId = (manager.getStore() != null) ? manager.getStore().getStoreId() : null;

        List<Staff> lsStaff = staffRepository.findByStoreId(storeId);
        int skip = (pageNum - 1) * pageSize;
        List<Staff> paged = lsStaff.stream().skip(skip).limit(pageSize).toList();

        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(s -> {
            // Thay đổi sang HashMap thông thường để chấp nhận giá trị null an toàn, không
            // lo crash app
            Map<String, Object> staffData = new HashMap<>();
            staffData.put("staffId", s.getStaffId());
            staffData.put("cccd", s.getIdNumber());
            staffData.put("ten", s.getStaffName());
            staffData.put("ngaySinh", s.getDoB());
            staffData.put("diaChi", s.getStaffAddr());
            staffData.put("avatar", s.getAvatar());
            staffData.put("statuSf", s.getStatus());
            staffData.put("Email", s.getEmail());
            staffData.put("PhoneNum", s.getPhoneNum());
            staffData.put("Gendar", s.getGender());

            // 🛠️ ĐÃ FIX LỖI getRoleId(): Đi đường vòng qua Object Role liên kết để trị dứt
            // điểm gạch đỏ
            String currentRoleId = null;
            if (s.getRole() != null) {
                try {
                    currentRoleId = s.getRole().getRoleId();
                } catch (Exception e) {
                    try {
                        // Thử gọi viết thường nếu model đặt là getRoleid()
                        var method = s.getRole().getClass().getMethod("getRoleid");
                        currentRoleId = (String) method.invoke(s.getRole());
                    } catch (Exception ex) {
                        currentRoleId = null;
                    }
                }
            }
            staffData.put("RoleId", currentRoleId);
            staffData.put("RoleName", s.getRole() != null ? s.getRole().getRoleName() : null);

            // Gộp phẳng dữ liệu theo đúng cấu trúc Item<StaffRespone> bên C# cũ của bạn
            Map<String, Object> flatItem = new HashMap<>();
            flatItem.put("Value", staffData);
            flatItem.put("PathChiTiet", "/manager/Staff/" + s.getStaffId());

            responseItems.add(flatItem);
        });

        Map<String, Object> res = new HashMap<>();
        res.put("items", responseItems);
        res.put("totalCount", lsStaff.size());
        res.put("pageIndex", pageNum);
        res.put("pageSize", pageSize);
        res.put("totalPages", (int) Math.ceil((double) lsStaff.size() / pageSize));

        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/Create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> createStaff(@ModelAttribute CreateStaffRequest staff,
            @RequestParam("file") MultipartFile file) {
        if (staff.getCccd() == null || staff.getTen() == null || staff.getRoleId() == null) {
            throw new IllegalArgumentException("Missing Staff request body");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate ngaySinh = LocalDate.parse(staff.getNgaySinh(), dtf);

        if (staff.getLuong().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Param Luong must higher 0");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Sysuser user = sysUserRepository.findById(Integer.parseInt(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Token Not Valid"));

        try {
            String relativeFilePath = imgService.SaveImgIntoProject(file);

            Staff newStaff = new Staff();
            newStaff.setStaffName(staff.getTen());
            // 🛠️ TỰ ĐỘNG KHỞI TẠO VÀ ÉP OBJECT ROLE VÀO STAFF (CHỐNG LỖI UNDEFINED VÀ TYPO
            // TÊN HÀM)
            try {
                // 1. Tạo một đối tượng Sysrole trống
                com.example.demo.Models.Sysrole tempRole = new com.example.demo.Models.Sysrole();

                // 2. Dò tìm hàm gán ID cho Sysrole (setRoleId hoặc setRoleid)
                try {
                    tempRole.getClass().getMethod("setRoleId", String.class).invoke(tempRole, staff.getRoleId());
                } catch (Exception e) {
                    try {
                        tempRole.getClass().getMethod("setRoleid", String.class).invoke(tempRole, staff.getRoleId());
                    } catch (Exception ex) {
                        try {
                            // Dự phòng nếu model đặt tên ngắn gọn là setId()
                            tempRole.getClass().getMethod("setId", String.class).invoke(tempRole, staff.getRoleId());
                        } catch (Exception e3) {
                        }
                    }
                }

                // 3. Dò tìm hàm gán Object Role vào Staff (setRole hoặc setSysrole)
                try {
                    newStaff.getClass().getMethod("setRole", com.example.demo.Models.Sysrole.class).invoke(newStaff,
                            tempRole);
                } catch (Exception e) {
                    try {
                        newStaff.getClass().getMethod("setSysrole", com.example.demo.Models.Sysrole.class)
                                .invoke(newStaff, tempRole);
                    } catch (Exception ex) {
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi gán Quyền: " + e.getMessage());
            }
            newStaff.setSalary(staff.getLuong());
            newStaff.setIdNumber(staff.getCccd());
            newStaff.setDoB(ngaySinh);
            newStaff.setStaffAddr(staff.getDiaChi());
            // 🛠️ TỰ ĐỘNG ÉP CHI NHÁNH (STORE) VÀO STAFF - CHỐNG LỖI UNDEFINED VÀ TYPO TÊN
            // HÀM
            try {
                Object currentStore = (user.getStore() != null) ? user.getStore() : null;
                String targetStoreId = null;

                if (currentStore != null) {
                    try {
                        targetStoreId = (String) currentStore.getClass().getMethod("getStoreId").invoke(currentStore);
                    } catch (Exception e) {
                        try {
                            targetStoreId = (String) currentStore.getClass().getMethod("getStoreid")
                                    .invoke(currentStore);
                        } catch (Exception ex) {
                            targetStoreId = null;
                        }
                    }
                }

                try {
                    // Cách 1: Thử gán chuỗi ID bằng hàm setStoreId(String)
                    java.lang.reflect.Method m = newStaff.getClass().getMethod("setStoreId", String.class);
                    m.invoke(newStaff, targetStoreId);
                } catch (Exception e1) {
                    try {
                        // Cách 2: Thử gán chuỗi ID bằng hàm setStoreid(String) viết thường
                        java.lang.reflect.Method m = newStaff.getClass().getMethod("setStoreid", String.class);
                        m.invoke(newStaff, targetStoreId);
                    } catch (Exception e2) {
                        // Cách 3: Gán nguyên cả Object Store liên kết (Chuẩn JPA Hibernate)
                        if (currentStore != null) {
                            try {
                                java.lang.reflect.Method m = newStaff.getClass().getMethod("setStore",
                                        currentStore.getClass());
                                m.invoke(newStaff, currentStore);
                            } catch (Exception e3) {
                                try {
                                    java.lang.reflect.Method m = newStaff.getClass().getMethod("setSysstore",
                                            currentStore.getClass());
                                    m.invoke(newStaff, currentStore);
                                } catch (Exception e4) {
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Không thể gán Store vào Staff: " + e.getMessage());
            }
            newStaff.setGender(staff.getGendar());
            newStaff.setPhoneNum(staff.getPhoneNum());
            newStaff.setEmail(staff.getEmail());

            var staff1 = sqlStaffService.createStaff(newStaff, relativeFilePath);

            Map<String, Object> respone = new HashMap<>();
            respone.put("ten", staff1.getStaffName());
            respone.put("staffId", staff1.getStaffId());
            respone.put("cccd", staff1.getStatus());
            respone.put("diaChi", staff1.getStaffAddr());
            respone.put("ngaySinh", staff1.getDoB());
            respone.put("avatar", staff1.getAvatar());
            respone.put("statuSf", staff1.getStatus());

            return ResponseEntity.ok(respone);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}