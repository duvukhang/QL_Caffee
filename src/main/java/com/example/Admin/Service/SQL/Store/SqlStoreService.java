package com.example.Admin.Service.SQL.Store;

import com.example.Admin.DTOS.Respone.PageResponse;
import com.example.Admin.DTOS.SqlDTO.StoreReportDto;
import com.example.Admin.Models.Store;

public interface SqlStoreService {
    Store createStore(Store newStore);
    
    int updateStoreStatus(String storeId, String newStatus);
    
    PageResponse<StoreReportDto> getStorePaging(int pageNum, int pageSize, String storeName, String storeAddr, String status);
    
    int updateStoreInfor(String storeId, String storeName, String storeAddr, String phoneNum);
}