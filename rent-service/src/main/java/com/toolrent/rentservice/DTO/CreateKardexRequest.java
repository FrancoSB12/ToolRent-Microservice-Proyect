package com.toolrent.rentservice.DTO;

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
    private String employeeRun;
}

