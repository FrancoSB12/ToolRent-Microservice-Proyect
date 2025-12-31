package com.toolrent.rentservice.Controller;

import com.toolrent.rentservice.Entity.RentXToolItemEntity;
import com.toolrent.rentservice.Service.RentXToolItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rent-x-tool-item")
public class RentXToolItemController {
    private final RentXToolItemService rentXToolItemService;

    @Autowired
    public RentXToolItemController(RentXToolItemService rentXToolItemService) {
        this.rentXToolItemService = rentXToolItemService;
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/tool-history/{toolItemId}")
    public ResponseEntity<?> getHistoryByToolId(@PathVariable Long toolItemId){
        List<RentXToolItemEntity> toolItemHistory = rentXToolItemService.getHistoryByToolId(toolItemId);
        return new ResponseEntity<>(toolItemHistory, HttpStatus.OK);
    }
}
