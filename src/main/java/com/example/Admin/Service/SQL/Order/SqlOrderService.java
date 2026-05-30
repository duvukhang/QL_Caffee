package com.example.Admin.Service.SQL.Order;

import java.util.List;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Order;

public interface SqlOrderService {
    // Trả về chuẩn object Order thay vì Object chung chung
    Order taoDon(String makhach, int maNv, List<HoaDonRequest.ProductItem> dssp);
    
    Order updateDonStatus(String id, String status);
}
