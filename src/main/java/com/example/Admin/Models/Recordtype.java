package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "recordtype", schema = "management")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Recordtype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TypeId")
    private Integer typeId;

    @Column(name = "TypeName", length = 100, nullable = false)
    private String typeName;

    @OneToMany(mappedBy = "type")
    private Collection<Inventoryrecord> inventoryrecords = new ArrayList<>();
}