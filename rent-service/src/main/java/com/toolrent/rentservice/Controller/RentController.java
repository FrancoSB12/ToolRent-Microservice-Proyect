package com.toolrent.rentservice.Controller;

import com.toolrent.rentservice.DTO.ToolRankingProjection;
import com.toolrent.rentservice.Entity.RentEntity;
import com.toolrent.rentservice.Service.RentService;
import com.toolrent.rentservice.Service.RentValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rent")
public class RentController {
    private final RentService rentService;
    private final RentValidationService rentValidationService;

    @Autowired
    public RentController(RentService rentService, RentValidationService rentValidationService) {
        this.rentService = rentService;
        this.rentValidationService = rentValidationService;
    }

    //Create loan
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping
    public ResponseEntity<?> createRent(@RequestBody RentEntity rent, Authentication authentication){
        //It's verified that the rent doesn't exist
        if(rent.getId() != null && rentService.exists(rent.getId())){
            return new ResponseEntity<>("El préstamo ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        String currentEmployeeRun;
        //This is to get the employee run from the keycloak
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            currentEmployeeRun = jwt.getClaimAsString("preferred_username");
        } else {
            currentEmployeeRun = authentication.getName();
        }

        //It's verified that the client exist
        if(!rentService.existsClient(rent.getClientRun())){
            return new ResponseEntity<>("Cliente no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        //It's verified that the employee exist
        if(!rentService.existsEmployee(currentEmployeeRun)){
            return new ResponseEntity<>("Empleado no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        //The data is validated for accuracy
        if(rentValidationService.isInvalidDate(rent.getRentDate())){
            return new ResponseEntity<>("Fecha de arriendo incorrecta", HttpStatus.BAD_REQUEST);
        }

        if(rentValidationService.isInvalidReturnDate(rent.getReturnDate(), rent.getRentDate())){
            return new ResponseEntity<>("Fecha de devolución incorrecta", HttpStatus.BAD_REQUEST);
        }

        try {
            RentEntity newLoan = rentService.createRent(rent, currentEmployeeRun);
            return new ResponseEntity<>(newLoan, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    //Get rent
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping
    public ResponseEntity<List<RentEntity>> getAllRents(){
        List<RentEntity> rents = rentService.getAllRents();
        return new ResponseEntity<>(rents, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/{id}")
    public ResponseEntity<RentEntity> getRentById(@PathVariable Long id){
        return rentService.getRentById(id)
                .map(rent -> new ResponseEntity<>(rent, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/client/{run}")
    public ResponseEntity<?> getActiveRentsByClient(@PathVariable String run){
        //It's verified that the client exist
        if(!rentService.existsClient(run)){
            return new ResponseEntity<>("Cliente no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        List<RentEntity> rents = rentService.getActiveRentsByClient(run);
        return new ResponseEntity<>(rents, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RentEntity>> getRentByStatus(@PathVariable String status){
        List<RentEntity> rents = rentService.getRentByStatus(status);
        return new ResponseEntity<>(rents, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/overdue")
    public ResponseEntity<List<RentEntity>> getOverdueRents(){
        List<RentEntity> overdue = rentService.getRentByReturnDateBeforeAndValidity();
        return new ResponseEntity<>(overdue, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/validity/{validity}")
    public ResponseEntity<List<RentEntity>> getRentByValidity(@PathVariable String validity){
        List<RentEntity> rents = rentService.getRentByValidity(validity);
        return new ResponseEntity<>(rents, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/stats/ranking")
    public ResponseEntity<List<ToolRankingProjection>> getToolRanking(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to){
        //Default range (all history)
        if (from == null) from = LocalDate.of(2000, 1, 1);
        if (to == null) to = LocalDate.now();

        List<ToolRankingProjection> ranking = rentService.getToolRanking(from, to);
        return new ResponseEntity<>(ranking, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/most-rented-tools")
    public ResponseEntity<List<Map<String, Object>>> getMostRentedTools(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return new ResponseEntity<>(rentService.getMostRentedTools(start, end), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PutMapping("/return/{id}")
    public ResponseEntity<?> returnRent(@PathVariable Long id, @RequestBody RentEntity rent){
        try {
            //It's verified that the rent exist
            Optional<RentEntity> existingRent = rentService.getRentById(id);
            if (existingRent.isEmpty()) {
                return new ResponseEntity<>("El préstamo no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            //It's verified that the client exist
            if(!rentService.existsClient(rent.getClientRun())){
                return new ResponseEntity<>("Cliente no encontrado!!!!!!!!!!!!!!! en la base de datos", HttpStatus.NOT_FOUND);
            }

            //It's verified that the employee exist
            if(!rentService.existsEmployee(rent.getEmployeeRun())){
                return new ResponseEntity<>("Empleado no encontrado en la base de datos", HttpStatus.NOT_FOUND);
            }

            if (existingRent.get().getStatus().equals("Finalizado")) {
                return new ResponseEntity<>("El préstamo ya está finalizado", HttpStatus.CONFLICT);
            }

            RentEntity returnedLoan = rentService.returnRent(id, rent);
            return new ResponseEntity<>(returnedLoan, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PutMapping("/update-late-statuses")
    public ResponseEntity<Void> checkAndSetLateStatuses(){
        rentService.checkAndSetLateStatuses();
        return ResponseEntity.noContent().build();
    }
}
