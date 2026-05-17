package com.example.demo.Controller.Manager;


import com.example.demo.MidWare.Filter.CustomError;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.SysUserRepository;
import com.example.demo.Service.GGService.SheetService; // Đảm bảo import đúng gói dịch vụ Google Sheets

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/review")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReviewManagerController {

    private final SheetService sheetService;
    private final SysUserRepository sysUserRepository;

    public ReviewManagerController(SheetService sheetService, SysUserRepository sysUserRepository) {
        this.sheetService = sheetService;
        this.sysUserRepository = sysUserRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStoreReviews() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Not Found User In Token");
        }

        Sysuser manager = sysUserRepository.findById(Integer.parseInt(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Token Not Valid"));

        String storeId = (manager.getStore() != null) ? manager.getStore().getStoreId() : null;
        if (storeId == null || storeId.isEmpty()) {
            throw new CustomError(403, "Forbidden", "User Fake");
        }

        try {
            // Gọi SheetService đọc dữ liệu mảng hai chiều (List của các mảng String) từ Google Sheets API
            List<String[]> data = sheetService.StoreReview(storeId);
            
            List<Map<String, Object>> reviewsList = new ArrayList<>();
            for (String[] item : data) {
                // Tái hiện logic xử lý chuỗi rỗng mặc định từ C# cũ của bạn
                String customer = (item[1] == null || item[1].isEmpty()) ? "Ẩn danh" : item[1];
                String comment = (item[4] == null || item[4].isEmpty()) ? "Không bình luận" : item[4];
                
                int rate;
                try {
                    rate = Integer.parseInt(item[3]);
                } catch (Exception e) {
                    rate = 0;
                }

                Map<String, Object> review = new HashMap<>();
                review.put("Time", item[0]);
                review.put("Custommer", customer);
                review.put("Rating", rate);
                review.put("Staffs", item[2]);
                review.put("comment", comment);
                
                reviewsList.add(review);
            }

            if (reviewsList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("StoreId", storeId);
            response.put("reviews", reviewsList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
