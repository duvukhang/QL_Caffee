package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "CustomerDetail",
    schema = "customers",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CustomerDetail_Email", columnNames = "Email"),
        @UniqueConstraint(name = "UQ_CustomerDetail_PhoneNum", columnNames = "PhoneNum")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetail {

    @Id
    @Column(name = "CustomerId", length = 10, nullable = false, columnDefinition = "char(10)")
    private String customerId;

    @Column(name = "FullName", length = 50)
    private String fullName;

    @Column(name = "IdNumber", length = 11, columnDefinition = "char(11)")
    private String idNumber;

    @Column(name = "Gender", length = 5)
    private String gender;

    @Column(name = "PhoneNum", length = 10)
    private String phoneNum;

    @Column(name = "Email", length = 50)
    private String email;

    @Column(name = "Avatar", length = 500, nullable = false)
    private String avatar;

    @Column(name = "Addr", length = 100)
    private String addr;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;
}
