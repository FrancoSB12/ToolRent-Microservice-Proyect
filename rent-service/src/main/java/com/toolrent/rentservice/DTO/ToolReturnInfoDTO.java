package com.toolrent.rentservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolReturnInfoDTO {
    private Long toolItemId;
    private String damageLevel;
}
