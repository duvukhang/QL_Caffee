package com.example.demo.DTOS.Respone;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DanhMucResponse {
    private String maLoaiDm;
    private String ten;
    private List<DMRes> danhMucCon = new ArrayList<>();

    @Data
    public static class DMRes {
        private String maDm;
        private String tenDM;
    }
}