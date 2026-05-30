package com.example.Admin.DTOS.Respone;

import lombok.Data;

@Data
public class TonKhoResponse {
    private String goodId;
    private String goodName;
    private String unitName;
    private int inStock;
    private String status;
}
