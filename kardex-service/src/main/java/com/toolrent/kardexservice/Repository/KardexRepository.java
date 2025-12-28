package com.toolrent.kardexservice.Repository;

import com.toolrent.kardexservice.Entity.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    //Hay que ver donde va esto, pero la idea es que el front llame a toolType, obtenga el id en base al
    //nombre y envie el id al kardex (en ese caso a lo mejor aqui va
    //Optional<List<KardexEntity>> findByToolTypeId(Long idToolType);)
    //Optional<List<KardexEntity>> findByToolType_Name(String toolTypeName);

    List<KardexEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
