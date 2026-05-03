package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.EquipmentReplacement;
import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentReplacementRepository extends JpaRepository<EquipmentReplacement, Long> {
    // composite index: object_type + object_id + replacement_date DESC
    List<EquipmentReplacement> findByObjectTypeAndObjectIdOrderByReplacementDateDesc(ObjectType objectType, Long objectId);
    // warranty expiry alerts — idx_replace_warranty
    List<EquipmentReplacement> findByWarrantyUntilBetween(LocalDate from, LocalDate to);
    List<EquipmentReplacement> findByMaintenanceRecord_MaintenanceId(Long maintenanceId);
}
