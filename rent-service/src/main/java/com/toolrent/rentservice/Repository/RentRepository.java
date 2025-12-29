package com.toolrent.rentservice.Repository;

import com.toolrent.rentservice.Entity.RentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentRepository extends JpaRepository<RentEntity, Long> {
    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools rt " +
            "WHERE r.id = :id")
    Optional<RentEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools " +
            "WHERE r.clientRun = :clientRun " +
            "AND r.status = 'Activo'")
    List<RentEntity> findActiveLoansByClient(@Param("run") String clientRun);

    @Query("SELECT r FROM RentEntity r " +
            "WHERE r.clientRun = :clientRun " +
            "AND r.status = 'Activo' " +
            "AND r.returnDate < :today")
    List<RentEntity> findOverdueLoans(@Param("clientRun") String clientRun, @Param("today") LocalDate today);

    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools rt")
    List<RentEntity> findAllWithDetails();

    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools lt " +
            "WHERE r.status = :status")
    List<RentEntity> findByStatusWithDetails(@Param("status") String status);

    List<RentEntity> findByValidity(String validity);
}
