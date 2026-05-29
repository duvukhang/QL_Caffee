package com.example.Admin.MidWare.Filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomError extends RuntimeException {
    private int status;
    private String error;

    public CustomError(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }
}
