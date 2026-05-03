package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {
    Optional<DeviceType> findByTypeCode(String typeCode);
    List<DeviceType> findByCategory(String category);
}
