package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "Customer",
    schema = "customers",
    uniqueConstraints = @UniqueConstraint(name = "Unique_UserName", columnNames = "UserName")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name = "CustomerId", length = 10, nullable = false, columnDefinition = "char(10)")
    private String customerId;

    @Column(name = "UserName", length = 50, nullable = false)
    private String userName;

    @Column(name = "Password", length = 100, nullable = false)
    private String password;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private CustomerDetail customerDetail;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
