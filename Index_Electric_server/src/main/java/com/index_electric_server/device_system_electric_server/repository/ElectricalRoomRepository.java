package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.ElectricalRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricalRoomRepository extends JpaRepository<ElectricalRoom, Long> {
    List<ElectricalRoom> findByBuilding_BuildingId(Long buildingId);
}
