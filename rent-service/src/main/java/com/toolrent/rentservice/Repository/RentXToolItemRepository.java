package com.toolrent.rentservice.Repository;

import com.toolrent.rentservice.Entity.RentXToolItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface RentXToolItemRepository extends JpaRepository<RentXToolItemEntity, Long> {
    List<RentXToolItemEntity> findByRent_Id(Long loanId);

    boolean existsByRent_Id(Long loanId);

    @Query("SELECT DISTINCT rxt.toolTypeId " +
            "FROM RentXToolItemEntity rxt " +
            "JOIN rxt.rent r " +
            "WHERE r.clientRun = :clientRun " +
            "AND r.status = 'Activo'")
    Set<Long> findActiveRentsToolTypeIdsByClient(@Param("clientRun") String clientRun);

    //Find the lastest rents for a specific tool, sort by descending loan ID or by date (the highest is the newest)
    @Query("SELECT rxt FROM RentXToolItemEntity rxt " +
            "WHERE rxt.toolItemId = :toolId " +
            "ORDER BY rxt.rent.rentDate DESC")
    List<RentXToolItemEntity> findHistoryByToolId(@Param("toolId") Long toolId);

    @Query("SELECT rxt.toolNameSnapshot, COUNT(rxt) " +
            "FROM RentXToolItemEntity rxt " +
            "WHERE rxt.rent.rentDate BETWEEN :start AND :end " +
            "GROUP BY rxt.toolNameSnapshot " +
            "ORDER BY COUNT(rxt) DESC")
    List<Object[]> findMostLoanedToolsBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
