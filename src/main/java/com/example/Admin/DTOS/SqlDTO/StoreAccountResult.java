package com.example.Admin.DTOS.SqlDTO;

import lombok.Data;

@Data
public class StoreAccountResult {
    private String username;
    private String roleId;
    private String staffName;
    private String staffId;
    private String storeId;
    private String storeName;
    private String eligibleStaff;
}
