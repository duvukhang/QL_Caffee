package com.example.Admin.Controller.Manager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.Admin.DTOS.Request.HoaDonRequest.ProductItem;
import com.example.Admin.Models.Inventoryrecord;
import com.example.Admin.Models.Stock;
import com.example.Admin.Models.Sysuser;
import com.example.Admin.Repositories.InventoryRecordRepository;
import com.example.Admin.Repositories.StockRepository;
import com.example.Admin.Repositories.SysUserRepository;
import com.example.Admin.Service.SQL.WarehouseImport.SqlWarehouseImportService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/kho")
@PreAuthorize("hasAnyAuthority('Admin', 'Manager', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class WarehouseManagerController {

    private final SqlWarehouseImportService sqlPhieuNhapKhoService;
    private final StockRepository stockRepository;
    private final InventoryRecordRepository inventoryRecordRepository;

    public WarehouseManagerController(SqlWarehouseImportService sqlPhieuNhapKhoService,
            StockRepository stockRepository,
            InventoryRecordRepository inventoryRecordRepository) {
        this.sqlPhieuNhapKhoService = sqlPhieuNhapKhoService;
        this.stockRepository = stockRepository;
        this.inventoryRecordRepository = inventoryRecordRepository;
    }

    @GetMapping("/Detail")
    public ResponseEntity<?> getDetailKho() {
        // ĐÃ BỎ: Check StoreId
        // Lấy tất cả hàng trong kho thay vì của chi nhánh
        List<Stock> stocks = stockRepository.findAll();

        List<Map<String, Object>> stockList = new ArrayList<>();
        final Object[] khoId = { 0 };

        stocks.forEach(item -> {
            Map<String, Object> map = new HashMap<>();
            if (item.getGood() != null) {
                map.put("GoodId", item.getGood().getGoodId());
                map.put("GoodName", item.getGood().getGoodName());
                map.put("UnitName", item.getGood().getUnitName());
            }

            map.put("InStock", item.getQuantity() != null ? item.getQuantity() : 0);
            map.put("Status", item.getStatus());
            stockList.add(map);

            if (item.getInventory() != null && item.getInventory().getInventoryId() != null) {
                khoId[0] = item.getInventory().getInventoryId();
            }
        });

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("InventoryId", khoId[0]);
        responseBody.put("Stock", stockList);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/Stock/{InventoryId}/{TypeId}")
    public ResponseEntity<?> updateStock(
            @PathVariable("InventoryId") int inventoryId,
            @PathVariable("TypeId") int typeId,
            @RequestBody List<ProductItem> requests) {
        try {
            // Gọi Service thực thi Stored Procedure qua cấu trúc mảng TVP
            Inventoryrecord res = sqlPhieuNhapKhoService.CreateInventoryRecords(requests, inventoryId, typeId);

            if (res != null) {
                List<Map<String, Object>> tonKhoResponses = new ArrayList<>();

                // 🛠️ ĐÃ CHUẨN HÓA: Duyệt mảng trực tiếp thông qua hàm getRecorDetails() từ
                // Model xịn
                if (res.getRecorDetails() != null) {
                    res.getRecorDetails().forEach(detailItem -> {
                        Map<String, Object> map = new HashMap<>();
                        try {
                            var good = detailItem.getGood();
                            if (good != null) {
                                map.put("GoodId", good.getGoodId());
                                map.put("GoodName", good.getGoodName());
                                map.put("UnitName", good.getUnitName());
                            }
                            map.put("InStock", detailItem.getQuantity());
                            tonKhoResponses.add(map);
                        } catch (Exception e) {
                        }
                    });
                }

                // 🛠️ ĐÃ CHUẨN HÓA: Đi đường vòng qua Object Inventory liên kết để lấy ID chi
                // nhánh công nghiệp
                Object safeInventoryId = 0;
                if (res.getInventory() != null) {
                    try {
                        safeInventoryId = res.getInventory().getClass().getMethod("getInventoryId")
                                .invoke(res.getInventory());
                    } catch (Exception e) {
                        try {
                            safeInventoryId = res.getInventory().getClass().getMethod("getInventoryid")
                                    .invoke(res.getInventory());
                        } catch (Exception ex) {
                        }
                    }
                }

                // Tạo duy nhất 1 Map phản hồi, dọn sạch hoàn toàn các đoạn code trùng lặp/thừa
                // phía sau
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("RecordId", res.getRecordsId());
                responseBody.put("InventoryId", safeInventoryId);
                responseBody.put("AdmissionDate", res.getAdmissionDate());
                responseBody.put("Detail", tonKhoResponses);

                return ResponseEntity.ok(responseBody);
            }
            throw new RuntimeException("Can't update Stock in Database");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/Record")
    public ResponseEntity<?> getRecord(@RequestParam("PageNum") int pageNum, @RequestParam("PageSize") int pageSize) {
        List<Inventoryrecord> records = inventoryRecordRepository.findAll();
        int skip = (pageNum - 1) * pageSize;
        List<Inventoryrecord> paged = records.stream().skip(skip).limit(pageSize).toList();

        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(item -> {
            Map<String, Object> val = new HashMap<>();
            val.put("RecordId", item.getRecordsId());
            val.put("AdmissionDate", item.getAdmissionDate());

            // 🛠️ ĐÃ CHUẨN HÓA: Truy xuất mã loại qua Object item.getType() liên kết của
            // Model mới
            Object currentTypeId = null;
            String currentTypeName = null;

            if (item.getType() != null) {
                currentTypeName = item.getType().getTypeName();
                try {
                    currentTypeId = item.getType().getClass().getMethod("getTypeId").invoke(item.getType());
                } catch (Exception e) {
                    try {
                        currentTypeId = item.getType().getClass().getMethod("getTypeid").invoke(item.getType());
                    } catch (Exception ex) {
                    }
                }
            }
            val.put("TypeId", currentTypeId);
            val.put("TypeName", currentTypeName);

            Map<String, Object> flatItem = new HashMap<>();
            flatItem.put("Value", val);
            flatItem.put("PathChiTiet", "Record/" + item.getRecordsId());

            responseItems.add(flatItem);
        });

        Map<String, Object> res = new HashMap<>();
        res.put("items", responseItems);
        res.put("totalCount", records.size());
        res.put("totalPages", (int) Math.ceil((double) records.size() / pageSize));
        res.put("pageIndex", pageNum);
        res.put("pageSize", pageSize);

        return ResponseEntity.ok(res);
    }
}
