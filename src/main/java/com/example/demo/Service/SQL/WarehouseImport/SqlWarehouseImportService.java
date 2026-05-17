package com.example.demo.Service.SQL.WarehouseImport;

import com.example.demo.DTOS.Request.HoaDonRequest.ProductItem;
import com.example.demo.Models.Inventoryrecord;
import java.util.List;

public interface SqlWarehouseImportService {
    
    // ĐÃ BỔ SUNG: Định nghĩa hàm tạo phiếu nhập kho trả về thực thể Inventoryrecord
    Inventoryrecord CreateInventoryRecords(List<ProductItem> requests, int inventoryId, int typeId) throws Exception;

    // Hàm alias dự phòng viết thường theo chuẩn camelCase của Java
    default Inventoryrecord createInventoryRecords(List<ProductItem> requests, int inventoryId, int typeId) throws Exception {
        return CreateInventoryRecords(requests, inventoryId, typeId);
    }
}