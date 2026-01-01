package com.toolrent.reportservice.Controller;

import com.toolrent.reportservice.DTO.RentReportDTO;
import com.toolrent.reportservice.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/active-rents")
    public ResponseEntity<?> getActiveRents(){
        try {
            List<RentReportDTO> activeRentsReport = reportService.getActiveRentsReport();
            return new ResponseEntity<>(activeRentsReport, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
