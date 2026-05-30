package com.example.Admin.Service.SQL.Staff;

import java.util.List;

import com.example.Admin.DTOS.Request.UpdateStaffRequest;
import com.example.Admin.DTOS.Respone.PageResponse3;
import com.example.Admin.DTOS.SqlDTO.StoreAccount;
import com.example.Admin.Models.Staff;

public interface SqlStaffService {
    
    Staff createStaff(Staff newStaff, String imgPath);
    
    int softDeleteUser(String id);
    
    // Hàm alias dự phòng viết hoa theo phong cách C# cũ để tránh lỗi gạch đỏ ở Controller
    default int SoftDeleteUser(String id) {
        return softDeleteUser(id);
    }
    
    String assignUserToStaff(String staffId, String userName);
    
    int updateStaffInfo(UpdateStaffRequest staffNewInfo);
    
    List<StoreAccount> getStoreAccountsAsync(String storeId, String roleId);
    
    PageResponse3<StoreAccount> getPageAccountAsync(int pageNum, int pageSize);
}