package com.example.demo.Service.SQL.Order;

import com.example.demo.Models.Order;
import com.example.demo.DTOS.Request.HoaDonRequest;
import java.util.List;

public interface SqlOrderService {
    // Trả về chuẩn object Order thay vì Object chung chung
    Order taoDon(String makhach, int maNv, List<HoaDonRequest.ProductItem> dssp);
    
    Order updateDonStatus(String id, String status);
}
