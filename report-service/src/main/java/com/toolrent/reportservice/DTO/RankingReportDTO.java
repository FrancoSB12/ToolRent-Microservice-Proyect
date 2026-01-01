package com.toolrent.reportservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingReportDTO {
    private String toolName;
    private Long toolTypeId;
    private Long totalRents;
}
