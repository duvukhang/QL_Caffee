package com.example.Admin.Models.Key;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StockId implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private Integer inventory;
    private String good;
}
