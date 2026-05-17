package com.example.demo.DTOS.SqlDTO;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class StoreAccount {
    private String username;
    private String roleId;
    private String staffId;
    private String staffName;
    private String storeId;
    private String storeName;
    
    private List<EligibleStaff> eligibleStaff = new ArrayList<>();
}
