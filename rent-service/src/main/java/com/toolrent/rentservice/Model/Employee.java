package com.toolrent.rentservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private String run;
    private String name;
    private String surname;
    private String email;
    private String cellphone;
    private boolean isAdmin;
}
