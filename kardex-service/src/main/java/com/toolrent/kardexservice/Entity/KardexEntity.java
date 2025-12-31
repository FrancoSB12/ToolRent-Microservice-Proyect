package com.toolrent.kardexservice.Entity;


import com.toolrent.kardexservice.Enum.KardexOperationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private KardexOperationType operationType;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "stock_involved", nullable = false)
    private Integer stockInvolved;

    @Column(name = "tool_type_name", nullable = false)
    private String toolTypeName;
}
