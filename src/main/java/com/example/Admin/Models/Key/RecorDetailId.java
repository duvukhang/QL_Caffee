package com.example.Admin.Models.Key;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



// File: RecorDetailId.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RecorDetailId implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String good;    // Khớp tên biến bên Entity
    private String records; // Khớp tên biến bên Entity
}

