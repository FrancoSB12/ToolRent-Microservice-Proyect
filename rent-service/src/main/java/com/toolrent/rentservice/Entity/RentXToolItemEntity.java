package com.toolrent.rentservice.Entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rent_x_tool_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentXToolItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rent_id", referencedColumnName = "id")
    private RentEntity rent;

    @Column(name = "tool_item_id", nullable = false)
    private Long toolItemId;

    @Column(name = "tool_type_id", nullable = false)
    private Long toolTypeId;

    //A "photo" of the important details is saved at the time of rental to keep them in case of any changes
    @Column(name = "tool_serial_snapshot")
    private String serialNumberSnapshot;

    @Column(name = "tool_name_snapshot")
    private String toolNameSnapshot;
}
