package com.example.Admin.Controller.Cashier;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Order;
import com.example.Admin.Models.Sysuser;
import com.example.Admin.Repositories.OrderRepository;
import com.example.Admin.Repositories.SysUserRepository;
import com.example.Admin.Service.SQL.Order.SqlOrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
public class OrderController {

    private final SqlOrderService sqlOrderService;
    private final SysUserRepository sysUserRepository;
    private final OrderRepository orderRepository;

    public OrderController(SqlOrderService sqlOrderService, SysUserRepository sysUserRepository, OrderRepository orderRepository) {
        this.sqlOrderService = sqlOrderService;
        this.sysUserRepository = sysUserRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @PreAuthorize("permitAll()") 
    public ResponseEntity<?> createOrder(@RequestBody HoaDonRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is empty.");
        }
        if (request.getMakhach() == null || request.getMakhach().isEmpty()) {
            request.setMakhach("CTM0000001");
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName() : "7";

            Order donhang = sqlOrderService.taoDon(request.getMakhach(), Integer.parseInt(userId), request.getDssp());

            if (donhang == null) {
                throw new RuntimeException("Failed to create order in database.");
            }

            List<Map<String, Object>> chiTietDonHang = new ArrayList<>();
            donhang.getOrderDetails().forEach(item -> {
                Map<String, Object> chiTiet = new HashMap<>();
                chiTiet.put("masp", item.getProduct() != null ? item.getProduct().getProductId() : null);
                chiTiet.put("tenSP", item.getProduct() != null ? item.getProduct().getProductName() : null);
                chiTiet.put("SoLuong", item.getQuantity());
                chiTiet.put("DonGia", item.getProduct() != null ? item.getProduct().getPrice() : null);
                chiTietDonHang.add(chiTiet);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("donhang", donhang.getOrderId());
            response.put("NgayNhan", donhang.getRecivingDate());
            response.put("ChiTiet", chiTietDonHang);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/Unproccess")
    public ResponseEntity<?> getOrderUnproccess() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                throw new RuntimeException("Token not valid"); 
            }

            String userId = auth.getName();
            Sysuser user = sysUserRepository.findById(Integer.parseInt(userId)).orElse(null);

            if (user == null) {
                throw new RuntimeException("This is not my Token");
            }

            // 🛠️ ĐÃ FIX: Dò tìm StoreId động thông qua Reflection để tránh lỗi getStoreId() / getStoreid()
            String userStoreId = null;
            if (user != null) {
                try {
                    Object storeObj = user.getClass().getMethod("getStore").invoke(user);
                    if (storeObj != null) {
                        try {
                            userStoreId = (String) storeObj.getClass().getMethod("getStoreId").invoke(storeObj);
                        } catch (Exception e) {
                            userStoreId = (String) storeObj.getClass().getMethod("getStoreid").invoke(storeObj);
                        }
                    }
                } catch (Exception e) {
                    userStoreId = null;
                }
            }

            List<Order> items = orderRepository.findByStatusAndSysUser_StoreId("Tiếp nhận", userStoreId);

            if (items == null || items.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            List<Map<String, Object>> responseItems = new ArrayList<>();
            for (Order item : items) {
                Map<String, Object> itemNew = new HashMap<>();
                itemNew.put("maDon", item.getOrderId());
                itemNew.put("TrangTHai", item.getStatus());
                itemNew.put("NgayNhan", item.getRecivingDate());
                itemNew.put("NgayHoangThanh", item.getCompleteDate());
                itemNew.put("PathChiTiet", "/manager/DH/chitiet/" + item.getOrderId());

                Sysuser orderUser = null;
                try {
                    java.lang.reflect.Method m = item.getClass().getMethod("getSysUser");
                    orderUser = (Sysuser) m.invoke(item);
                } catch (Exception e1) {
                    try {
                        java.lang.reflect.Method m = item.getClass().getMethod("getSysuser");
                        orderUser = (Sysuser) m.invoke(item);
                    } catch (Exception e2) {
                        try {
                            java.lang.reflect.Method m = item.getClass().getMethod("getUser");
                            orderUser = (Sysuser) m.invoke(item);
                        } catch (Exception e3) {
                            // Mối quan hệ không tồn tại
                        }
                    }
                }

                String usernameStr = null;
                if (orderUser != null) {
                    try {
                        java.lang.reflect.Method m = orderUser.getClass().getMethod("getUserName");
                        usernameStr = (String) m.invoke(orderUser);
                    } catch (Exception e1) {
                        try {
                            java.lang.reflect.Method m = orderUser.getClass().getMethod("getUsername");
                            usernameStr = (String) m.invoke(orderUser);
                        } catch (Exception e2) {
                            usernameStr = null;
                        }
                    }
                }
                itemNew.put("User", usernameStr);

                List<Map<String, Object>> ctdhList = new ArrayList<>();
                item.getOrderDetails().forEach(item2 -> {
                    Map<String, Object> ctdh = new HashMap<>();
                    ctdh.put("masp", item2.getProduct() != null ? item2.getProduct().getProductId() : null);
                    ctdh.put("tenSP", item2.getProduct() != null ? item2.getProduct().getProductName() : null);
                    ctdh.put("SoLuong", item2.getQuantity());
                    ctdh.put("Gia", item2.getProduct() != null ? item2.getProduct().getPrice() : null);
                    ctdh.put("ThanhTiem", item2.getProduct() != null ? 
                            java.math.BigDecimal.valueOf(item2.getQuantity()).multiply(item2.getProduct().getPrice()) : null);
                    ctdhList.add(ctdh);
                });
                itemNew.put("CTDH", ctdhList);
                responseItems.add(itemNew);
            }

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("Items", responseItems);

            return ResponseEntity.ok(finalResponse);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/Unproccess")
    public ResponseEntity<?> updateDonStatus(@RequestParam("id") String id, @RequestParam("status") String status) {
        if (id == null || id.isEmpty() || status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Missing Param Madon or Status");
        }
        try {
            if ("Hủy".equals(status)) {
                status = "Đã hủy";
            }
            var response = sqlOrderService.updateDonStatus(id, status);
            if (response == null) {
                throw new RuntimeException("Can't update status in database");
            }
            return ResponseEntity.ok(Map.of("message", "Update status successfully"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}