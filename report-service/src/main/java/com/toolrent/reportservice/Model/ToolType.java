package com.toolrent.reportservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolType {
    private Long id;
    private String name;
    private String category;
    private String model;
    private Integer replacementValue;
    private Integer totalStock;
    private Integer availableStock;
    private Integer rentalFee;
    private Integer damageFee;
}
