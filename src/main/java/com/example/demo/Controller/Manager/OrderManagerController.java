package com.example.demo.Controller.Manager;

import com.example.demo.Models.Order;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.SysUserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/DH")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class OrderManagerController {

    private final OrderRepository orderRepository;
    private final SysUserRepository sysUserRepository;

    public OrderManagerController(OrderRepository orderRepository, SysUserRepository sysUserRepository) {
        this.orderRepository = orderRepository;
        this.sysUserRepository = sysUserRepository;
    }

    @GetMapping("/TheoNgay/{datestart}/{dateend}/{pageNum}/{pageSize}")
    public ResponseEntity<?> getOrdersByDate(
            @PathVariable("datestart") String datestart,
            @PathVariable("dateend") String dateend,
            @PathVariable("pageNum") int pageNum,
            @PathVariable("pageSize") int pageSize) {

        if (datestart == null || datestart.isEmpty() || dateend == null || dateend.isEmpty()) {
            throw new IllegalArgumentException("Missing Param datestart or dateend");
        }

        // Tái hiện chính xác định dạng format dd-MM-yyyy từ C#
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(datestart + " 00:00:00", formatter);
            end = LocalDateTime.parse(dateend + " 23:59:59", formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Date Format not true");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        Sysuser user = sysUserRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new RuntimeException("User not exists in Server"));

        String storeId = (user.getStore() != null) ? user.getStore().getStoreId() : null;

        // Bạn cần thêm hàm này vào OrderRepository để tối ưu truy vấn phân trang:
        // List<Order> findByRecivingDateBetweenAndSysUser_StoreId(LocalDateTime start, LocalDateTime end, String storeId);
        // long countByRecivingDateBetweenAndSysUser_StoreId(LocalDateTime start, LocalDateTime end, String storeId);
        List<Order> items = orderRepository.findByRecivingDateBetweenAndSysUser_StoreId(start, end, storeId);

        if (items == null || items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Thực hiện phân trang thủ công bằng Stream Skip/Limit giống LINQ của C#
        int skip = (pageNum - 1) * pageSize;
        List<Order> pagedItems = items.stream().skip(skip).limit(pageSize).toList();

        List<Map<String, Object>> responseItems = new ArrayList<>();
        for (Order item : pagedItems) {
            Map<String, Object> itemNew = new HashMap<>();
            itemNew.put("maDon", item.getOrderId());
            itemNew.put("TrangTHai", item.getStatus());
            itemNew.put("NgayNhan", item.getRecivingDate());
            itemNew.put("NgayHoangThanh", item.getCompleteDate());
            
            String username = null;
            try {
                Object sysUserObj = item.getClass().getMethod("getSysUser").invoke(item);
                username = (String) sysUserObj.getClass().getMethod("getUserName").invoke(sysUserObj);
            } catch (Exception e) {
                // Né lỗi Null nếu quan hệ trống
            }
            itemNew.put("User", username);
            itemNew.put("PathChiTiet", "/manager/DH/chitiet/" + item.getOrderId());

            List<Map<String, Object>> ctdhList = new ArrayList<>();
            item.getOrderDetails().forEach(detail -> {
                Map<String, Object> ctdh = new HashMap<>();
                ctdh.put("masp", detail.getProduct() != null ? detail.getProduct().getProductId() : null);
                ctdh.put("tenSP", detail.getProduct() != null ? detail.getProduct().getProductName() : null);
                ctdh.put("SoLuong", detail.getQuantity());
                ctdh.put("Gia", detail.getProduct() != null ? detail.getProduct().getPrice() : BigDecimal.ZERO);
                
                BigDecimal thanhTien = detail.getProduct() != null ? 
                        BigDecimal.valueOf(detail.getQuantity()).multiply(detail.getProduct().getPrice()) : BigDecimal.ZERO;
                ctdh.put("ThanhTiem", thanhTien);
                ctdhList.add(ctdh);
            });
            itemNew.put("CTDH", ctdhList);
            responseItems.add(itemNew);
        }

        long totalCount = items.size();
        Map<String, Object> response = new HashMap<>();
        response.put("Items", responseItems);
        response.put("PageIndex", pageNum);
        response.put("PageSize", pageSize);
        response.put("TotalCount", totalCount);
        response.put("TotalPages", (int) Math.ceil((double) totalCount / pageSize));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/Chitiet/{MaDon}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("MaDon") String maDon) {
        Order donHang = orderRepository.findById(maDon)
                .orElseThrow(() -> new RuntimeException("The bill doesn't exists"));

        List<Map<String, Object>> ctdhList = new ArrayList<>();
        final BigDecimal[] tongCong = {BigDecimal.ZERO};

        donHang.getOrderDetails().forEach(ct -> {
            Map<String, Object> ctdh = new HashMap<>();
            ctdh.put("masp", ct.getProduct() != null ? ct.getProduct().getProductId() : null);
            ctdh.put("tenSP", ct.getProduct() != null ? ct.getProduct().getProductName() : null);
            ctdh.put("SoLuong", ct.getQuantity());
            ctdh.put("Gia", ct.getProduct() != null ? ct.getProduct().getPrice() : BigDecimal.ZERO);
            
            BigDecimal thanhTien = ct.getProduct() != null ? 
                    BigDecimal.valueOf(ct.getQuantity()).multiply(ct.getProduct().getPrice()) : BigDecimal.ZERO;
            ctdh.put("ThanhTiem", thanhTien);
            ctdhList.add(ctdh);
            
            tongCong[0] = tongCong[0].add(thanhTien);
        });

        // Tính tổng tiền bao gồm 10% thuế VAT (Nhân với 1.1)
        BigDecimal totalWithVat = tongCong[0].multiply(BigDecimal.valueOf(1.1));

        Map<String, Object> response = new HashMap<>();
        response.put("OrderId", donHang.getOrderId());
        response.put("OrderDate", donHang.getRecivingDate());
        response.put("CompleteDate", donHang.getCompleteDate());
        response.put("customer", donHang.getCustomerId());
        response.put("Status", donHang.getStatus());
        response.put("Total", totalWithVat);
        response.put("VatTax", "10%");
        response.put("Detail", ctdhList);

        return ResponseEntity.ok(response);
    }
}