package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.DeviceCheckDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceCheckDefinitionRepository extends JpaRepository<DeviceCheckDefinition, Long> {

    // composite index: device_type_id + sort_order — no filesort
    List<DeviceCheckDefinition> findByDeviceType_DeviceTypeIdOrderBySortOrder(Long deviceTypeId);

    Optional<DeviceCheckDefinition> findByDeviceType_DeviceTypeIdAndItemCode(Long deviceTypeId, String itemCode);

    // Load definitions + image markers in 1 query (for DeviceDetailPage)
    @Query("""
        SELECT cd FROM DeviceCheckDefinition cd
        LEFT JOIN FETCH cd.imageMarkers im
        LEFT JOIN FETCH im.deviceTypeImage dti
        WHERE cd.deviceType.deviceTypeId = :typeId
          AND dti.imageId = :imageId
        ORDER BY cd.sortOrder
        """)
    List<DeviceCheckDefinition> findWithMarkersByTypeAndImage(
        @Param("typeId") Long typeId,
        @Param("imageId") Long imageId
    );
}
