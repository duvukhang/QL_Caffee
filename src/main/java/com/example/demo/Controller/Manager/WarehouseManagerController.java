package com.example.demo.Controller.Manager;

import com.example.demo.DTOS.Request.HoaDonRequest.ProductItem;
import com.example.demo.Models.Inventoryrecord;
import com.example.demo.Models.Stock;
import com.example.demo.Models.Sysuser;
import com.example.demo.Repositories.InventoryRecordRepository;
import com.example.demo.Repositories.StockRepository;
import com.example.demo.Repositories.SysUserRepository;
import com.example.demo.Service.SQL.WarehouseImport.SqlWarehouseImportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/kho")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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

            Object inStockVal = 0;
            try {
                inStockVal = item.getClass().getMethod("getInStock").invoke(item);
            } catch (Exception e1) {
                try {
                    inStockVal = item.getClass().getMethod("getInstock").invoke(item);
                } catch (Exception e2) {
                    inStockVal = 0;
                }
            }
            map.put("InStock", inStockVal);
            map.put("Status", item.getStatus());
            stockList.add(map);

            try {
                khoId[0] = item.getClass().getMethod("getInventoryId").invoke(item);
            } catch (Exception e) {
                try {
                    khoId[0] = item.getClass().getMethod("getInventoryid").invoke(item);
                } catch (Exception ex) {
                }
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