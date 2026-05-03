package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.Equipment;
import com.index_electric_server.device_system_electric_server.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // QR scan → lookup by equipment_code (UNIQUE index)
    Optional<Equipment> findByEquipmentCode(String equipmentCode);

    List<Equipment> findByBuilding_BuildingIdAndStatus(Long buildingId, Status status);

    List<Equipment> findByDeviceType_DeviceTypeId(Long deviceTypeId);

    // Load equipment + check definitions in 1 query — avoids N+1
    @Query("""
        SELECT DISTINCT e FROM Equipment e
        JOIN FETCH e.deviceType dt
        JOIN FETCH dt.checkDefinitions cd
        WHERE e.equipmentCode = :code
        ORDER BY cd.sortOrder
        """)
    Optional<Equipment> findWithCheckDefinitionsByCode(@Param("code") String code);

    // Last check per equipment — for dashboard
    @Query("""
        SELECT e FROM Equipment e
        LEFT JOIN FETCH e.checks c
        WHERE e.building.buildingId = :buildingId
          AND e.status = 'active'
        """)
    List<Equipment> findActiveByBuildingWithChecks(@Param("buildingId") Long buildingId);
}
