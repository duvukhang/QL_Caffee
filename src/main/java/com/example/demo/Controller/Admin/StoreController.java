package com.example.demo.Controller.Admin;

// Thêm các thư viện lõi của Spring Web và Security
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Thêm các class DTO và Service của project (Đường dẫn có thể thay đổi tùy thư mục của bạn)
import com.example.demo.DTOS.Request.CreateStoreRequest;
import com.example.demo.Models.Store;
import com.example.demo.Service.SQL.Store.SqlStoreService;

import java.util.Map;

@RestController
@RequestMapping("/Admin/Store")
@PreAuthorize("hasRole('ADMIN')")
public class StoreController {

    private final SqlStoreService sqlStoreService;

    public StoreController(SqlStoreService sqlStoreService) {
        this.sqlStoreService = sqlStoreService;
    }

    @GetMapping
    public ResponseEntity<?> getStore(
            @RequestParam int pageNum, @RequestParam int pageSize,
            @RequestParam(defaultValue = "") String storeName,
            @RequestParam(defaultValue = "") String storeAddr,
            @RequestParam(defaultValue = "") String storeStatus) {
        
        var response = sqlStoreService.getStorePaging(pageNum, pageSize, storeName, storeAddr, storeStatus);
        if (response == null) throw new RuntimeException("Can't get Data in Database");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody CreateStoreRequest request) {
        if (request == null || request.getDiaChi() == null || request.getDiaChi().isEmpty()
                || request.getSdt() == null || request.getSdt().isEmpty()
                || request.getTenCh() == null || request.getTenCh().isEmpty()) {
            throw new IllegalArgumentException("This request need 3 Attribute: SDT, TenCh, DiaChi");
        }
        if (request.getDiaChi().length() > 50 || request.getSdt().length() > 11 || request.getTenCh().length() > 50) {
            throw new IllegalArgumentException("Length of attribute is out of range");
        }
        
        // ĐÃ FIX LỖI Ở ĐÂY: Tạo một object Store mới và copy dữ liệu từ Request sang
        Store newStore = new Store();
        newStore.setStoreName(request.getTenCh());
        newStore.setStoreAddr(request.getDiaChi());
        newStore.setPhoneNum(request.getSdt());

        // Truyền newStore vào thay vì request
        var response = sqlStoreService.createStore(newStore);
        
        if (response == null) throw new RuntimeException("Fail to Create Store In Database");
        return ResponseEntity.ok(Map.of("message", "Create Successfully"));
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<?> updateStoreStatus(@PathVariable String storeId, @RequestParam String newstatus) {
        if (storeId == null || storeId.isEmpty()) throw new IllegalArgumentException("Missing Param storeId");
        int status = sqlStoreService.updateStoreStatus(storeId, newstatus);
        if (status == 500) throw new RuntimeException("Can't Update Status ");
        if (status == 404) throw new RuntimeException("Store not Exists in Database");
        return ResponseEntity.ok(Map.of("message", "Update Status Succesfully"));
    }
}