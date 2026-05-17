package com.example.demo.Service.SQL.Store;

import com.example.demo.DTOS.Respone.PageResponse;
import com.example.demo.DTOS.SqlDTO.StoreReportDto;
import com.example.demo.Models.Store;
import com.example.demo.Repositories.StoreRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SqlStoreServiceImpl implements SqlStoreService {

    private final StoreRepository storeRepository;
    private final EntityManager entityManager;

    public SqlStoreServiceImpl(StoreRepository storeRepository, EntityManager entityManager) {
        this.storeRepository = storeRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional // Tự động quản lý Commit/Rollback thay cho transaction manual của C#
    public Store createStore(Store newStore) {
        try {
            Store store = new Store();
            store.setStoreId(generateId("CH"));
            store.setStoreName(newStore.getStoreName());
            store.setStoreAddr(newStore.getStoreAddr());
            store.setPhoneNum(newStore.getPhoneNum());
            store.setStoreStatus("Hoạt Động");
            
            return storeRepository.save(store);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public PageResponse<StoreReportDto> getStorePaging(int pageNum, int pageSize, String storeName, String storeAddr, String status) {
        
        // 1. Sử dụng EntityManager để gọi Stored Procedure
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_GetStoreReport_Pagination", StoreReportDto.class);
        
        // 2. Đăng ký các tham số IN và OUT
        query.registerStoredProcedureParameter("PageNumber", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PageSize", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SearchName", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SearchStatus", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SearchAddress", String.class, ParameterMode.IN);
        
        // Tham số OUTPUT tương đương ParameterDirection.Output của C#
        query.registerStoredProcedureParameter("TotalRecords", Integer.class, ParameterMode.OUT); 

        // 3. Truyền giá trị vào các tham số IN
        query.setParameter("PageNumber", pageNum);
        query.setParameter("PageSize", pageSize);
        query.setParameter("SearchName", storeName);
        query.setParameter("SearchStatus", status);
        query.setParameter("SearchAddress", storeAddr);

        // 4. Thực thi truy vấn
        query.execute();

        // 5. Lấy danh sách kết quả và giá trị của biến OUTPUT
        List<StoreReportDto> data = query.getResultList();
        Integer totalRecords = (Integer) query.getOutputParameterValue("TotalRecords");
        
        if (totalRecords == null) {
            totalRecords = 0;
        }

        // 6. Trả về format DTO phân trang
        PageResponse<StoreReportDto> response = new PageResponse<>();
        response.setItems(data);
        response.setTotalCount(totalRecords);
        response.setPageIndex(pageNum);
        response.setPageSize(pageSize);
        response.setTotalPages((int) Math.ceil((double) totalRecords / pageSize));

        return response;
    }

    @Override
    @Transactional
    public int updateStoreInfor(String storeId, String storeName, String storeAddr, String phoneNum) {
        try {
            Store updateStore = storeRepository.findById(storeId).orElse(null);
            if (updateStore == null) {
                return 404;
            }
            
            updateStore.setStoreName(storeName);
            updateStore.setStoreAddr(storeAddr);
            updateStore.setPhoneNum(phoneNum);
            
            // JPA sẽ tự động hiểu EntityState.Modified khi gọi save() trên một object đã tồn tại
            storeRepository.save(updateStore); 
            return 200;
        } catch (Exception e) {
            return 500;
        }
    }

    @Override
    @Transactional
    public int updateStoreStatus(String storeId, String newStatus) {
        try {
            Store deleteStore = storeRepository.findById(storeId).orElse(null);
            if (deleteStore == null) {
                return 404;
            }
            
            deleteStore.setStoreStatus(newStatus);
            storeRepository.save(deleteStore);
            return 200;
        } catch (Exception e) {
            return 500;
        }
    }

    // Hàm sinh ID tự động như hệ thống cũ
    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}