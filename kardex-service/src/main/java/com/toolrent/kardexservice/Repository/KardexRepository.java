package com.toolrent.kardexservice.Repository;

import com.toolrent.kardexservice.Entity.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    Optional<List<KardexEntity>> findByToolTypeName(String toolTypeName);

    List<KardexEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
