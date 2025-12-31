package com.toolrent.rentservice.DTO;

import com.toolrent.rentservice.Model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyLateFeeRequest {
    private LocalDate returnDate;
    private Integer lateReturnFee;
    private Client client;
}
