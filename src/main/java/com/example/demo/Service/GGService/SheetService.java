package com.example.demo.Service.GGService;

import java.util.List;

public interface SheetService {
    
    // ĐÃ THÊM: Khai báo cấu trúc hàm đọc dữ liệu đánh giá cửa hàng từ Google Sheets
    List<String[]> StoreReview(String storeId) throws Exception;
}