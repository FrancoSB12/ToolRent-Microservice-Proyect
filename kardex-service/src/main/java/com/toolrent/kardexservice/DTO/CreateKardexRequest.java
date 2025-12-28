package com.toolrent.kardexservice.DTO;

import com.toolrent.kardexservice.Enum.KardexOperationType;
import lombok.Data;

@Data
public class CreateKardexRequest {
    private Long toolTypeId;
    private KardexOperationType operationType;
    private Integer stock;
}
