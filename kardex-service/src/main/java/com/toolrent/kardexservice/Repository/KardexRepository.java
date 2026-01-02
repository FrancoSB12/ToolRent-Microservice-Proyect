package com.toolrent.kardexservice.Repository;

import com.toolrent.kardexservice.Entity.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    Optional<List<KardexEntity>> findByToolTypeName(String toolTypeName);

    List<KardexEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Transactional
    @Query("UPDATE KardexEntity k SET k.toolTypeName = :newName WHERE k.toolTypeName = :name")
    void updateToolTypeName(@Param("name") String name, @Param("newName") String newName);
}
