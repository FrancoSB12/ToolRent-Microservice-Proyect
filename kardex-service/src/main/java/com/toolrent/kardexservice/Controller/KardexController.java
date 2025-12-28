package com.toolrent.kardexservice.Controller;

import com.toolrent.kardexservice.Entity.KardexEntity;
import com.toolrent.kardexservice.Service.KardexService;
import com.toolrent.kardexservice.Service.KardexValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin("*")
public class KardexController {
    private final KardexService kardexService;
    private final KardexValidationService kardexValidationService;

    @Autowired
    public KardexController(KardexService kardexService, KardexValidationService kardexValidationService) {
        this.kardexService = kardexService;
        this.kardexValidationService = kardexValidationService;
    }

    //Create kardex
    @PostMapping
    public ResponseEntity<?> createKardex(@RequestBody KardexEntity kardex) {
        //First, it's verified that the kardex doesn't exist
        if (kardex.getId() != null && kardexService.exists(kardex.getId())) {
            return new ResponseEntity<>("El kardex ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if(kardexValidationService.isInvalidOperationType(kardex.getOperationType().toString())){
            return new ResponseEntity<>("El tipo de operación es inválido", HttpStatus.BAD_REQUEST);
        }

        if(kardexValidationService.isInvalidDate(kardex.getDate())){
            return new ResponseEntity<>("La fecha es inválida", HttpStatus.BAD_REQUEST);
        }

        if(kardexValidationService.isInvalidStock(kardex.getStockInvolved())){
            return new ResponseEntity<>("El stock involucrado es inválido", HttpStatus.BAD_REQUEST);
        }

        KardexEntity newKardex = kardexService.saveKardex(kardex);
        return new ResponseEntity<>(newKardex, HttpStatus.CREATED);
    }

    //Get Kardex
    @GetMapping("/")
    public ResponseEntity<?> getAllKardex() {
        List<KardexEntity> kardexes = kardexService.getAllKardex();
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    //Get by tool name

    @GetMapping("/by-date")
    public ResponseEntity<?> getKardexByDateRange(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if(startDate.isAfter(endDate)){
            return new ResponseEntity<>("La fecha de inicio es posterior a la de finalización", HttpStatus.BAD_REQUEST);
        }

        List<KardexEntity> kardexes = kardexService.getKardexByDateRange(startDate, endDate);
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    //Delete kardex
    @DeleteMapping("/kardex/{id}")
    public ResponseEntity<String> deleteKardexById(@PathVariable Long id){
        boolean deletedKardex = kardexService.deleteKardexById(id);
        return deletedKardex ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
