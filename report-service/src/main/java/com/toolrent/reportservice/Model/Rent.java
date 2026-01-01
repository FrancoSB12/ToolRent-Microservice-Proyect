package com.toolrent.reportservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rent {
    private Long id;
    private LocalDate rentDate;
    private LocalTime rentTime;
    private LocalDate returnDate;
    private LocalTime returnTime;
    private Integer lateReturnFee;
    private String status;
    private String validity;
    private String clientRun;
    private String employeeRun;
    private List<RentXToolItem> rentTools;
    private String clientNameSnapshot;
    private String employeeNameSnapshot;
}
