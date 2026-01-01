package com.toolrent.inventoryservice.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ToolItemIdsRequest {
    List<Long> toolItemIds;
}
