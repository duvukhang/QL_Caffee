package com.example.demo.DTOS.Respone;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecordsInventoryResponse {
    private String recordId;
    private LocalDateTime admissionDate; // DateTime -> LocalDateTime
    private int typeId;
    private String typeName;
}
