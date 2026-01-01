package com.toolrent.kardexservice.DTO;

import com.toolrent.kardexservice.Enum.KardexOperationType;
import lombok.Data;

@Data
public class CreateKardexRequest {
    private String toolTypeName;
    private KardexOperationType operationType;
    private Integer stock;
    private String employeeRun;
}
