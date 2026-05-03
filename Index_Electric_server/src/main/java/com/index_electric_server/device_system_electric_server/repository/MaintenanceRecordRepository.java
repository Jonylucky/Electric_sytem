package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.MaintenanceRecord;
import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    // composite index: object_type + object_id + performed_date DESC — avoids N+1
    List<MaintenanceRecord> findByObjectTypeAndObjectIdOrderByPerformedDateDesc(ObjectType objectType, Long objectId);
    Optional<MaintenanceRecord> findByMaintenanceCode(String maintenanceCode);
    List<MaintenanceRecord> findByNextDueDateBetween(LocalDate from, LocalDate to);
    List<MaintenanceRecord> findByScheduledDateBetween(LocalDate from, LocalDate to);
}
