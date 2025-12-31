package com.toolrent.feeservice.Controller;

import com.toolrent.feeservice.DTO.ApplyLateFeeRequest;
import com.toolrent.feeservice.DTO.UpdateLateReturnFeeRequest;
import com.toolrent.feeservice.Service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fee")
public class FeeController {
    private final FeeService feeService;

    @Autowired
    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    //Late return fee get and set
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/current-late-return-fee")
    public ResponseEntity<Integer> getCurrentLateReturnFee() {
        return new ResponseEntity<>(feeService.getLateReturnFee(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/late-return-fee")
    public ResponseEntity<?> updateGlobalLateReturnFee(@RequestBody UpdateLateReturnFeeRequest lateFeeRequest) {
        if(lateFeeRequest.getNewLateReturnFee() == null ||  lateFeeRequest.getNewLateReturnFee() < 0) {
            return new ResponseEntity<>("El monto de la multa no puede ser negativo", HttpStatus.BAD_REQUEST);
        }

        feeService.updateGlobalLateReturnFee(lateFeeRequest);
        return new ResponseEntity<>("Tarifa actualizada para futuros préstamos", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PutMapping("/apply-late-return-fee")
    public ResponseEntity<?> applyLateReturnFee(@RequestBody ApplyLateFeeRequest applyLateFeeRequest) {
        feeService.applyLateReturnFee(applyLateFeeRequest);
        return new ResponseEntity<>("Multa por atraso aplicada al cliente", HttpStatus.OK);
    }
}
