package com.example.Admin.DTOS.Respone;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageResponse2 {
    private int pageIndex;
    private int pageSize;
    private int totalPages;
    private int totalCount;
    private List<Item2> items = new ArrayList<>();

    public boolean isHasPreviousPage() { return pageIndex > 1; }
    public boolean isHasNextPage() { return pageIndex < totalPages; }

    @Data
    public static class Item2 {
        private String maDon;
        private String trangTHai;
        private LocalDateTime ngayNhan;
        private LocalDateTime ngayHoangThanh;
        private String user;
        private List<CTDHResponse> ctdh = new ArrayList<>();
        private String pathChiTiet;
    }

    @Data
    public static class CTDHResponse {
        private String masp;
        private String tenSP;
        private int soLuong;
        private BigDecimal gia;
        private BigDecimal thanhTiem;
    }
}
