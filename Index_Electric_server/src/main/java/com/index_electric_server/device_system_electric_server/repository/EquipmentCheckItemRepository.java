package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.EquipmentCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentCheckItemRepository extends JpaRepository<EquipmentCheckItem, Long> {

    List<EquipmentCheckItem> findByEquipmentCheck_CheckId(Long checkId);

    // Out-of-range readings for a month — uses idx_items_definition
    @Query("""
        SELECT ci FROM EquipmentCheckItem ci
        JOIN FETCH ci.equipmentCheck ec
        JOIN FETCH ci.checkDefinition cd
        JOIN FETCH ec.equipment e
        WHERE ec.checkDate >= :from
          AND ci.numericValue IS NOT NULL
          AND (ci.numericValue < cd.minValue OR ci.numericValue > cd.maxValue)
        ORDER BY ec.checkDate DESC
        """)
    List<EquipmentCheckItem> findOutOfRangeSince(@Param("from") LocalDateTime from);

    // Trip count delta for ACB — window function equivalent in Java
    @Query("""
        SELECT ci FROM EquipmentCheckItem ci
        JOIN FETCH ci.equipmentCheck ec
        WHERE ec.equipment.equipmentId = :equipmentId
          AND ci.checkDefinition.checkDefinitionId = :definitionId
        ORDER BY ec.checkDate DESC
        LIMIT :limit
        """)
    List<EquipmentCheckItem> findNumericHistory(
        @Param("equipmentId") Long equipmentId,
        @Param("definitionId") Long definitionId,
        @Param("limit") int limit
    );
}
