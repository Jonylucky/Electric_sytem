package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.EquipmentCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentCheckRepository extends JpaRepository<EquipmentCheck, Long> {

    // composite index: equipment_id + check_date DESC
    List<EquipmentCheck> findByEquipment_EquipmentIdOrderByCheckDateDesc(Long equipmentId);

    // Check history with items — avoids N+1
    @Query("""
        SELECT DISTINCT ec FROM EquipmentCheck ec
        JOIN FETCH ec.items ci
        JOIN FETCH ci.checkDefinition cd
        WHERE ec.equipment.equipmentId = :equipmentId
          AND ec.checkDate >= :from
        ORDER BY ec.checkDate DESC
        """)
    List<EquipmentCheck> findWithItemsByEquipmentAndDateRange(
        @Param("equipmentId") Long equipmentId,
        @Param("from") LocalDateTime from
    );

    // Last check per equipment — for dashboard lateral-style
    @Query("""
        SELECT ec FROM EquipmentCheck ec
        WHERE ec.equipment.equipmentId = :equipmentId
        ORDER BY ec.checkDate DESC
        LIMIT 1
        """)
    Optional<EquipmentCheck> findLatestByEquipment(@Param("equipmentId") Long equipmentId);
}
