package com.toolrent.rentservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolItem {
    private Long id;
    private String serialNumber;
    private String status;
    private String damageLevel;
    private ToolType toolType;
}
