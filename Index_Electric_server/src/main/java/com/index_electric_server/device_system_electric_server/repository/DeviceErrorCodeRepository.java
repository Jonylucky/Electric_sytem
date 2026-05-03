package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.DeviceErrorCode;
import com.index_electric_server.device_system_electric_server.enums.SeverityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceErrorCodeRepository extends JpaRepository<DeviceErrorCode, Long> {
    // composite index: device_type_id + is_active
    List<DeviceErrorCode> findByDeviceType_DeviceTypeIdAndIsActiveTrueOrderBySeverityLevelDescErrorCodeAsc(Long deviceTypeId);
    Optional<DeviceErrorCode> findByDeviceType_DeviceTypeIdAndErrorCode(Long deviceTypeId, String errorCode);
    List<DeviceErrorCode> findByDeviceType_DeviceTypeIdAndSeverityLevel(Long deviceTypeId, SeverityLevel severityLevel);
}
