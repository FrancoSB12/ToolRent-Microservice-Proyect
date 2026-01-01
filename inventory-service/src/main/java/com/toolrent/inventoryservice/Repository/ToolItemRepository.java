package com.toolrent.inventoryservice.Repository;

import com.toolrent.inventoryservice.Entity.ToolItemEntity;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolItemRepository extends JpaRepository<ToolItemEntity, Long> {

    Optional<ToolItemEntity> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    Optional<ToolItemEntity> findFirstByToolType_IdAndStatusAndDamageLevelIn(Long toolTypeId, ToolStatus status, List<ToolDamageLevel> damageLevels);

    @Query("SELECT DISTINCT i.toolType " +
            "FROM ToolItemEntity i " +
            "WHERE i.id IN :ids")
    List<ToolTypeEntity> findToolTypesByToolItemIds(@Param("ids") List<Long> ids);
}
