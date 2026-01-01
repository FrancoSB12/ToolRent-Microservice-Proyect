package com.toolrent.reportservice.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OverdueClientsDTO {
    private String run;
    private String name;
    private String surname;
    private String email;
    private String cellphone;
    private Long rentId;
    private LocalDate rentDate;
    private LocalDate expectedReturnDate;
    private long daysOverdue;
}