package com.example.Admin.DTOS.Respone;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int status = 500;
    private String message = "";
    private String error = "";
    private String time = LocalDateTime.now().toString();
    private String path = "";
}
