package com.toolrent.kardexservice.Controller;

import com.toolrent.kardexservice.DTO.CreateKardexRequest;
import com.toolrent.kardexservice.Entity.KardexEntity;
import com.toolrent.kardexservice.Service.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/kardex")
public class KardexController {
    private final KardexService kardexService;

    @Autowired
    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    //Get Kardex
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping
    public ResponseEntity<?> getAllKardex() {
        List<KardexEntity> kardexes = kardexService.getAllKardex();
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    //Get by tool name
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/tool/{toolName}")
    public ResponseEntity<?> getKardexByToolName(@PathVariable String toolName) {
        return kardexService.getKardexByToolTypeName(toolName)
                .map(kardexes -> new ResponseEntity<>(kardexes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/by-date")
    public ResponseEntity<?> getKardexByDateRange(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if(startDate.isAfter(endDate)){
            return new ResponseEntity<>("La fecha de inicio es posterior a la de finalización", HttpStatus.BAD_REQUEST);
        }

        List<KardexEntity> kardexes = kardexService.getKardexByDateRange(startDate, endDate);
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    //Controllers that only communicates with other microservices
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping("/entry")
    public void createKardexEntry(@RequestBody CreateKardexRequest request) {
        kardexService.createKardexEntry(request);
    }
}
