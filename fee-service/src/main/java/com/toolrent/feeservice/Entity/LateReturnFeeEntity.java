package com.toolrent.feeservice.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "late-return-fee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LateReturnFeeEntity {
    //This entity manages the late return fee for the loan, so when the admin wants to update, loans that have already been made won't be affected

    @Id
    private Long id = 1L;

    @Column(name = "current_late_return_fee", nullable = false)
    private Integer currentLateReturnFee;
}
