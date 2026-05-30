package com.example.Admin.Models;

import com.example.Admin.Models.Key.RecorDetailId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@EqualsAndHashCode
@Entity
@Table(name = "RecorDetail", schema = "management")
@IdClass(RecorDetailId.class)
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class RecorDetail {
    @Id
    @ManyToOne 
    @JoinColumn(name = "GoodId")
    private Good good;

    @Id
    @ManyToOne 
    @JoinColumn(name = "RecordsId")
    private Inventoryrecord records;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;
}
