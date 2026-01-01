package com.toolrent.reportservice.Controller;

import com.toolrent.reportservice.DTO.ActiveRentsReportDTO;
import com.toolrent.reportservice.DTO.OverdueClientsDTO;
import com.toolrent.reportservice.DTO.RankingReportDTO;
import com.toolrent.reportservice.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
            List<ActiveRentsReportDTO> activeRentsReport = reportService.getActiveRentsReport();
            return new ResponseEntity<>(activeRentsReport, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/overdue-clients")
    public ResponseEntity<List<OverdueClientsDTO>> getOverdueClients(){
        List<OverdueClientsDTO> overdueClients = reportService.getOverdueClientsReport();
        return new ResponseEntity<>(overdueClients, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/ranking-tools")
    public ResponseEntity<List<RankingReportDTO>> getRankingReport(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<RankingReportDTO> rankingReport = reportService.getToolRanking(from, to);
        return new ResponseEntity<>(rankingReport, HttpStatus.OK);
    }
}
