package com.example.Admin.DTOS.Request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateStaffRequest {
    private String staffId;
    private String staffName;
    private String staffIdNumber;
    private String address;
    private LocalDate dob; // DateOnly -> LocalDate
    private String email;
    private String phoneNum;
    private String gender;
    private String roleid;
}