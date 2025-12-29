package com.toolrent.inventoryservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateKardexRequest {
    private Long toolTypeId;
    private String operationType;
    private Integer stock;
}
