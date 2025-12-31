package com.toolrent.inventoryservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateToolTypeRequest {
    private Long id;
    private String name;
    private String category;
    private String model;
    private Integer totalStock;
    private Integer availableStock;
}
