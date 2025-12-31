package com.toolrent.feeservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private String run;
    private String name;
    private String surname;
    private String email;
    private String cellphone;
    private String status;
    private Integer debt;
    private Integer activeRents;
}
