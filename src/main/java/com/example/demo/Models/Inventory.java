package com.example.demo.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "inventory", schema = "management")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryId")
    private Integer inventoryId;

    @Column(name = "Status", length = 100, nullable = false)
    private String status;

    @Column(name = "Addr", length = 100, nullable = false)
    private String addr;

    // 🛠️ ĐÃ FIX: Chuyển từ String thuần sang mối quan hệ Đối Tượng @ManyToOne 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StoreID", referencedColumnName = "StoreID", nullable = false)
    private Store store; // Biến này khớp 100% với thuộc tính mappedBy = "store" bên Store.java!

    @OneToMany(mappedBy = "inventory")
    private Collection<Inventoryrecord> inventoryrecords = new ArrayList<>();

    @OneToMany(mappedBy = "inventory")
    private Collection<Stock> stocks = new ArrayList<>();
}