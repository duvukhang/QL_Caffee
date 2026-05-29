package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "sysrole", schema = "management")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sysrole {

    @Id
    @Column(name = "RoleId", length = 10, columnDefinition = "char(10)", nullable = false)
    private String roleId;

    @Column(name = "RoleName", length = 50, nullable = false)
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Staff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Sysuser> sysusers = new ArrayList<>();
}
