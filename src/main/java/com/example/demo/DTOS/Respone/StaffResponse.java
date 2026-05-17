package com.example.demo.DTOS.Respone;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffResponse {
    private String staffId;
    private String cccd;
    private String ten;
    private String diaChi;
    private LocalDate ngaySinh; // DateOnly -> LocalDate
    private String gendar;
    private String email;
    private String avatar;
    private String statuSf;
    private String phoneNum;
    private String roleId;
    private String roleName;
}
