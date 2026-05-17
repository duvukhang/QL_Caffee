package com.example.demo.Models;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "goods", schema = "management")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Good {
    @Id
    @Column(name = "GoodId", length = 10, columnDefinition = "char(10)")
    private String goodId;

    @Column(name = "GoodName", length = 100, nullable = false)
    private String goodName;

    @Column(name = "UnitName", length = 20, nullable = false)
    private String unitName;

    @OneToMany(mappedBy = "good")
    private Collection<RecorDetail> recorDetails = new ArrayList<>();

    @OneToMany(mappedBy = "good")
    private Collection<Stock> stocks = new ArrayList<>();
}
