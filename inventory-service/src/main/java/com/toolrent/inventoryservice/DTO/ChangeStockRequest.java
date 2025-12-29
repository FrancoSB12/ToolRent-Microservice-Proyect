package com.toolrent.inventoryservice.DTO;

import lombok.Data;

@Data
public class ChangeStockRequest {
    private Long id;
    private Integer quantity;
}
