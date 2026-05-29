package com.example.Admin.Models;

import com.example.Admin.Models.Key.StockId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Stock", schema = "management")
@IdClass(StockId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @ManyToOne
    @JoinColumn(name = "InventoryId")
    private Inventory inventory;

    @Id
    @ManyToOne
    @JoinColumn(name = "GoodId")
    private Good good;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Status", length = 100)
    private String status;
}
