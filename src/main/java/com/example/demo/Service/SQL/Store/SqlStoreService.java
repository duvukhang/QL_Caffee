package com.example.demo.Service.SQL.Store;

import com.example.demo.DTOS.Respone.PageResponse;
import com.example.demo.DTOS.SqlDTO.StoreReportDto;
import com.example.demo.Models.Store;

public interface SqlStoreService {
    Store createStore(Store newStore);
    
    int updateStoreStatus(String storeId, String newStatus);
    
    PageResponse<StoreReportDto> getStorePaging(int pageNum, int pageSize, String storeName, String storeAddr, String status);
    
    int updateStoreInfor(String storeId, String storeName, String storeAddr, String phoneNum);
}