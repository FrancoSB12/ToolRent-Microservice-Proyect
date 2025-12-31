package com.toolrent.feeservice.DTO;

import com.toolrent.feeservice.Model.Client;
import lombok.Data;

@Data
public class ChargeClientFeeRequest {
    private Client client;
    private Integer fee;
}