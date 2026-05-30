package com.example.Admin.Models.Key;

import lombok.*;
import java.io.Serializable;

// Class này bắt buộc phải implement Serializable và có EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderDetailId implements Serializable {
    // Tên 2 biến này phải TRÙNG KHỚP với tên biến relationship trong class OrderDetail bên dưới
    private String order;   
    private String product; 
}