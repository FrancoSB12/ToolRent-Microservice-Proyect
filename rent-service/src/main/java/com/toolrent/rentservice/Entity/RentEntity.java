package com.toolrent.rentservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "rent")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id" ,unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "rent_date", nullable = false)
    private LocalDate rentDate;

    @Column(name = "rent_time", nullable = false)
    private LocalTime rentTime;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "return_time")
    private LocalTime returnTime;

    @Column(name = "late_return_fee", nullable = false)
    private Integer lateReturnFee = 0;

    //The initial value of the status is always "Activo"
    @Column(name = "status", nullable = false)
    private String status = "Activo";

    //The initial value of the state is always "Vigente"
    @Column(name = "validity", nullable = false)
    private String validity = "Vigente";

    @Column(name = "client_run", nullable = false)
    private String clientRun;

    @Column(name = "employee_run", nullable = false)
    private String employeeRun;

    @OneToMany(mappedBy = "rent")
    private List<RentXToolItemEntity> rentTools;

    //A "photo" of the important details is saved so as not to constantly call other microservices
    @Column(name = "client_name_snapshot")
    private String clientNameSnapshot;

    @Column(name = "employee_name_snapshot")
    private String employeeNameSnapshot;

    public String getValidity() {
        if ("Finalizado".equalsIgnoreCase(this.status)) {
            return this.validity;
        }

        if (this.returnDate != null && LocalDate.now().isAfter(this.returnDate)) {
            return "Atrasado";
        }

        return "Puntual";
    }
}
