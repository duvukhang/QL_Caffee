package com.example.demo.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "staff", schema = "management")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @Column(name = "StaffId", length = 10, columnDefinition = "char(10)", nullable = false)
    private String staffId;

    @Column(name = "IdNumber", length = 11, columnDefinition = "char(11)", nullable = false)
    private String idNumber;

    @Column(name = "StaffName", length = 50, nullable = false)
    private String staffName;

    @Column(name = "StaffAddr", length = 50, nullable = false)
    private String staffAddr;

    @Column(name = "PhoneNum", length = 10, columnDefinition = "char(10)")
    private String phoneNum;

    @Column(name = "Email", length = 50)
    private String email;

    @Column(name = "DoB")
    private LocalDate doB; // DateOnly trong C# tương đương LocalDate trong Java

    @Column(name = "Salary", columnDefinition = "money", nullable = false)
    private BigDecimal salary;

    @Column(name = "Bonus", columnDefinition = "money", nullable = false)
    private BigDecimal bonus;

    @Column(name = "Gender", length = 5)
    private String gender;

    @Column(name = "Avatar", length = 500, nullable = false)
    private String avatar;

    @Column(name = "Status", length = 100)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StoreId")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleId", nullable = false)
    private Sysrole role;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Sysuser> sysusers = new ArrayList<>();
}
