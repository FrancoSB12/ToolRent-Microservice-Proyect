package com.toolrent.inventoryservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateKardexRequest {
    private String toolTypeName;
    private String operationType;
    private Integer stock;
}
