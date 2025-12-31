package com.toolrent.feeservice.DTO;

import com.toolrent.feeservice.Model.Client;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplyLateFeeRequest {
    private LocalDate returnDate;
    private Integer lateReturnFee;
    private Client client;
}
