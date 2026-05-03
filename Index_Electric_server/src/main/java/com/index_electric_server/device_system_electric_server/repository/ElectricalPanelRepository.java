package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.ElectricalPanel;
import com.index_electric_server.device_system_electric_server.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElectricalPanelRepository extends JpaRepository<ElectricalPanel, Long> {
    Optional<ElectricalPanel> findByPanelCode(String panelCode);
    List<ElectricalPanel> findByBuilding_BuildingIdAndStatus(Long buildingId, Status status);
    List<ElectricalPanel> findByParentPanel_PanelId(Long parentPanelId);
    List<ElectricalPanel> findByParentPanelIsNullAndBuilding_BuildingId(Long buildingId);

    // Load panel + all coords for drawing viewer
    @Query("""
        SELECT DISTINCT ep FROM ElectricalPanel ep
        JOIN FETCH ep.drawingCoords dc
        WHERE dc.drawingFile.drawingId = :drawingId
        ORDER BY dc.xPercent
        """)
    List<ElectricalPanel> findWithCoordsByDrawing(@Param("drawingId") Long drawingId);
}
