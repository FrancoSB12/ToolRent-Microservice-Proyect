package com.toolrent.inventoryservice.DTO;

import com.toolrent.inventoryservice.Model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeClientFeeRequest {
    private Client client;
    private Integer fee;
}
