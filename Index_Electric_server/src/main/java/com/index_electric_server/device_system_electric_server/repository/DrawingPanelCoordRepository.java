package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.DrawingPanelCoord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawingPanelCoordRepository extends JpaRepository<DrawingPanelCoord, Long> {
    // idx_coord_drawing — main DrawingViewer query
    List<DrawingPanelCoord> findByDrawingFile_DrawingIdOrderByXPercentAsc(Long drawingId);
    // find all drawings containing a given panel
    List<DrawingPanelCoord> findByPanel_PanelId(Long panelId);
    Optional<DrawingPanelCoord> findByDrawingFile_DrawingIdAndPanel_PanelId(Long drawingId, Long panelId);
}
