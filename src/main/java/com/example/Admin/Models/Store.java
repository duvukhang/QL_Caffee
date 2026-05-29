package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "store") // Không có schema management
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @Column(name = "StoreID", length = 10, columnDefinition = "char(10)", nullable = false)
    private String storeId;

    @Column(name = "StoreName", length = 100, nullable = false)
    private String storeName;

    @Column(name = "StoreAddr", length = 100, nullable = false)
    private String storeAddr;

    @Column(name = "PhoneNum", length = 11, columnDefinition = "char(11)")
    private String phoneNum;

    @Column(name = "Store_Status", length = 100)
    private String storeStatus;

    @Column(name = "Email", length = 100)
    private String email;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Staff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Sysuser> sysusers = new ArrayList<>();
}
