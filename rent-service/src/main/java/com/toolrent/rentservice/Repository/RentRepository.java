package com.toolrent.rentservice.Repository;

import com.toolrent.rentservice.DTO.ToolRankingProjection;
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
    List<RentEntity> findActiveRentsByClient(@Param("clientRun") String clientRun);

    @Query("SELECT r FROM RentEntity r " +
            "WHERE r.clientRun = :clientRun " +
            "AND r.status = 'Activo' " +
            "AND r.returnDate < :today")
    List<RentEntity> findClientOverdueRents(@Param("clientRun") String clientRun, @Param("today") LocalDate today);

    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools rt")
    List<RentEntity> findAllWithDetails();

    @Query("SELECT r FROM RentEntity r " +
            "LEFT JOIN FETCH r.rentTools rt " +
            "WHERE r.status = :status")
    List<RentEntity> findByStatusWithDetails(@Param("status") String status);

    List<RentEntity> findByReturnDateBeforeAndValidityAndStatus(LocalDate date, String validity, String status);

    List<RentEntity> findByValidity(String validity);

    @Query("SELECT i.toolTypeId as toolTypeId, COUNT(i) as count " +
            "FROM RentXToolItemEntity i " +
            "JOIN i.rent r " +
            "WHERE r.rentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY i.toolTypeId " +
            "ORDER BY count DESC")
    List<ToolRankingProjection> findTopRentedTools(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
