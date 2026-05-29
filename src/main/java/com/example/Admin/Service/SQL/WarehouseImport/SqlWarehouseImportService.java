package com.example.Admin.Service.SQL.WarehouseImport;

import java.util.List;

import com.example.Admin.DTOS.Request.HoaDonRequest.ProductItem;
import com.example.Admin.Models.Inventoryrecord;

public interface SqlWarehouseImportService {
    
    // ĐÃ BỔ SUNG: Định nghĩa hàm tạo phiếu nhập kho trả về thực thể Inventoryrecord
    Inventoryrecord CreateInventoryRecords(List<ProductItem> requests, int inventoryId, int typeId) throws Exception;

    // Hàm alias dự phòng viết thường theo chuẩn camelCase của Java
    default Inventoryrecord createInventoryRecords(List<ProductItem> requests, int inventoryId, int typeId) throws Exception {
        return CreateInventoryRecords(requests, inventoryId, typeId);
    }
}