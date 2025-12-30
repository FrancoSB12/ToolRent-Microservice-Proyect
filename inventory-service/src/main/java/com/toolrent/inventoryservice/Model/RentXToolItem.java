package com.toolrent.inventoryservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentXToolItem {
    private Long id;
    private Long rentId;
    private Long toolItemId;
    private Long toolTypeId;
    private String serialNumberSnapshot;
    private String toolNameSnapshot;
    private Integer agreedPrice;
}
