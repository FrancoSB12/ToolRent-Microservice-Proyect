package com.toolrent.employeeservice.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {
    @Id
    @Column(name = "run", length = 12, nullable = false, unique = true, updatable = false)    //The length is 12 because the hyphen and dots also counts
    private String run;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "cellphone", nullable = false)
    private String cellphone;

    @JsonProperty("isAdmin")
    @Column(name = "isAdmin", nullable = false)
    private boolean isAdmin;
}
