package com.toolrent.rentservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentReturnRequest {
    private List<ToolReturnInfoDTO> returnedTools;
    private String employeeRun;
}
