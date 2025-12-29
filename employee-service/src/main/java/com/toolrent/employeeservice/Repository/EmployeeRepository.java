package com.toolrent.employeeservice.Repository;

import com.toolrent.employeeservice.Entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {
    public List<EmployeeEntity> findByIsAdmin(boolean isAdmin);
}
