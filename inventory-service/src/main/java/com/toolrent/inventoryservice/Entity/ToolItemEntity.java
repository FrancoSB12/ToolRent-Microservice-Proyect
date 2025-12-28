package com.toolrent.inventoryservice.Entity;


import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "toolItem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolItemEntity {
    //This entity contains what makes that physical tool unique

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "serial_number", unique = true,  nullable = false, updatable = false)
    private String serialNumber;    //This will be used for the employee to search for the tool

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ToolStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_level", nullable = false)
    private ToolDamageLevel damageLevel;

    @ManyToOne(fetch = FetchType.EAGER)     //To automatically load parent data
    @JoinColumn(name = "tool_type_id", referencedColumnName = "id", nullable = false)
    private ToolTypeEntity toolType;
}
