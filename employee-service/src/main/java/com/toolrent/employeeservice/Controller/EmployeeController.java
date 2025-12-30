package com.toolrent.employeeservice.Controller;

import com.toolrent.employeeservice.Entity.EmployeeEntity;
import com.toolrent.employeeservice.Service.EmployeeService;
import com.toolrent.employeeservice.Service.EmployeeValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeValidationService employeeValidationService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeValidationService employeeValidationService) {
        this.employeeService = employeeService;
        this.employeeValidationService = employeeValidationService;
    }

    //Create employee
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeEntity employee){ //, @RequestParam String password)
        //First, it's verified that the employee doesn't exist
        if(employeeService.exists(employee.getRun())){
            return new ResponseEntity<>("El empleado ya existe", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if(employeeValidationService.isInvalidRun(employee.getRun())) {
            return new ResponseEntity<>("Run del empleado invalido", HttpStatus.BAD_REQUEST);
        }

        if(employeeValidationService.isInvalidName(employee.getName())){
            return new ResponseEntity<>("Nombre del empleado invalido", HttpStatus.BAD_REQUEST);
        }

        if(employeeValidationService.isInvalidName(employee.getSurname())){
            return new ResponseEntity<>("Apellido del empleado invalido", HttpStatus.BAD_REQUEST);
        }

        if(employeeValidationService.isInvalidEmail(employee.getEmail())){
            return new ResponseEntity<>("Email del empleado invalido", HttpStatus.BAD_REQUEST);
        }

        if(employeeValidationService.isInvalidCellphone(employee.getCellphone())){
            return new ResponseEntity<>("Teléfono del empleado invalido", HttpStatus.BAD_REQUEST);
        }

        EmployeeEntity newEmployee = employeeService.registerEmployee(employee);    //, password)
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    //Get employee
    @GetMapping
    public ResponseEntity<List<EmployeeEntity>> getAllEmployees(){
        List<EmployeeEntity> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/{employeeRun}")
    public ResponseEntity<EmployeeEntity> getEmployeeByRun(@PathVariable String employeeRun){
        return employeeService.getEmployeeByRun(employeeRun)
                .map(employee -> new ResponseEntity<>(employee, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/isAdmin")
    public ResponseEntity<List<EmployeeEntity>> getEmployeeByIsAdmin(){
        List<EmployeeEntity> employees = employeeService.getEmployeeIfItIsAdmin();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    //Update employee
    @PutMapping("/{run}")
    public ResponseEntity<?> updateEmployee(@PathVariable String run, @RequestBody EmployeeEntity employee){
        try {
            //Verify that the employee has a run and isn't null
            if (employee.getRun() == null || employee.getRun().isEmpty()) {
                return new ResponseEntity<>("El run no puede estar vacio o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the employee exist in the database
            if (employeeService.getEmployeeByRun(run).isEmpty()) {
                return new ResponseEntity<>("El empleado no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            EmployeeEntity updatedEmployee = employeeService.updateEmployee(run, employee);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Delete employee
    @DeleteMapping("/{employeeRun}")
    public ResponseEntity<String> deleteEmployeeByRun(@PathVariable("employeeRun") String run){
        boolean deletedEmployee = employeeService.deleteEmployeeByRun(run);
        return deletedEmployee ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
