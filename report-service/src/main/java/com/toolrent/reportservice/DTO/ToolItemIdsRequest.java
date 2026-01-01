package com.toolrent.reportservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolItemIdsRequest {
    List<Long> toolItemIds;
}
