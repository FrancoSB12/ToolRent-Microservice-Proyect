package com.toolrent.inventoryservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "toolType")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolTypeEntity {
    //This entity contains information that is the same for all tools in a model

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "replacement_value", nullable = false)
    private Integer replacementValue;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = 0;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock = 0;

    @Column(name = "rental_fee", nullable = false)
    private Integer rentalFee;

    @Column(name = "damage_fee", nullable = false)
    private Integer damageFee;
}
