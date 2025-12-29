package com.toolrent.clientservice.DTO;

import lombok.Data;

@Data
public class ChargeClientFeeRequest {
    private String clientRun;
    private Integer fee;
}
