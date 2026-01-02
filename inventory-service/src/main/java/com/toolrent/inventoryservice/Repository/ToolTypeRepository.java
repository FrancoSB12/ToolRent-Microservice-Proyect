package com.toolrent.inventoryservice.Repository;

import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolTypeRepository extends JpaRepository<ToolTypeEntity, Long> {

    Optional<ToolTypeEntity> findByName(String name);

    @Query("SELECT t FROM ToolTypeEntity t WHERE t.id IN :ids")
    List<ToolTypeEntity> findToolTypesByIds(@Param("ids") List<Long> ids);
}
