package com.toolrent.employeeservice.Service;

import com.toolrent.employeeservice.Entity.EmployeeEntity;
import com.toolrent.employeeservice.Repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    EmployeeValidationService employeeValidationService;
    EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeValidationService employeeValidationService, EmployeeRepository employeeRepository) {
        this.employeeValidationService = employeeValidationService;
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeEntity> getAllEmployees(){
        return employeeRepository.findAll();
    }

    public Optional<EmployeeEntity> getEmployeeByRun(String run){
        return employeeRepository.findById(run);
    }

    public List<EmployeeEntity> getEmployeeIfItIsAdmin(){
        return employeeRepository.findByIsAdmin(true);
    }

    public boolean exists(String run){
        return employeeRepository.existsById(run);
    }

    @Transactional
    public EmployeeEntity registerEmployee(EmployeeEntity employee){ //, String password)
        //Save in DB
        EmployeeEntity newEmployee = employeeRepository.save(employee);

        //Create Keycloak user
        //String userId = keycloakUserService.createUser(employee.getRun(), employee.getEmail(), employee.getName(), employee.getSurname(), password);

        //Assign role
        //String role = employee.isAdmin() ? "Admin" : "Employee";
        //keycloakUserService.assignRole(userId, role);
        return newEmployee;
    }

    public EmployeeEntity updateEmployee(String run, EmployeeEntity employee){
        //The employee is searched in the database
        Optional<EmployeeEntity> databaseEmployee = employeeRepository.findById(run);
        EmployeeEntity databaseEmployeeEntity = databaseEmployee.get();

        //Each attribute is checked to see which one was updated
        if(employee.getName() != null){
            if(employeeValidationService.isInvalidName(employee.getName())){
                throw new IllegalArgumentException("Nombre del empleado invalido");
            }
            databaseEmployeeEntity.setName(employee.getName());
        }

        if(employee.getSurname() != null){
            if(employeeValidationService.isInvalidName(employee.getSurname())){
                throw new IllegalArgumentException("Apellido del empleado invalido");
            }
            databaseEmployeeEntity.setSurname(employee.getSurname());
        }

        if(employee.getEmail() != null){
            if(employeeValidationService.isInvalidEmail(employee.getEmail())){
                throw new IllegalArgumentException("Email del empleado invalido");
            }
            databaseEmployeeEntity.setEmail(employee.getEmail());
        }

        if(employee.getCellphone() != null){
            if(employeeValidationService.isInvalidCellphone(employee.getCellphone())){
                throw new IllegalArgumentException("Teléfono del empleado invalido");
            }
            databaseEmployeeEntity.setCellphone(employee.getCellphone());
        }

        return employeeRepository.save(databaseEmployeeEntity);
    }

    public boolean deleteEmployeeByRun(String run){
        if(employeeRepository.existsById(run)){
            employeeRepository.deleteById(run);
            return true;

        } else{
            return false;
        }
    }
}
