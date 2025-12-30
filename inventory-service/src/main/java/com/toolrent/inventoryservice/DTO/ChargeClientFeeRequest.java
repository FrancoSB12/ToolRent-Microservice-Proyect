package com.toolrent.inventoryservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeClientFeeRequest {
    private String clientRun;
    private Integer fee;
}
