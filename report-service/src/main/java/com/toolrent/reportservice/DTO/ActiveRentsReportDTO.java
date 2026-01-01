package com.toolrent.reportservice.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ActiveRentsReportDTO {
    private Long rentId;
    private LocalDate rentDate;
    private String validity;
    private String clientName;
    private String clientRun;
    private List<String> toolNames;
}
