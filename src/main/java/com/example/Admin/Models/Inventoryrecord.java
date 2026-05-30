package com.example.Admin.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "inventoryrecord", schema = "management")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class Inventoryrecord {
    @Id
    @Column(name = "RecordsId", length = 10, columnDefinition = "char(10)")
    private String recordsId;

    @Column(name = "AdmissionDate", nullable = false)
    private LocalDateTime admissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InventoryId", nullable = false)
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TypeId", nullable = false)
    private Recordtype type;

    @OneToMany(mappedBy = "records")
    private Collection<RecorDetail> recorDetails = new ArrayList<>();
}
