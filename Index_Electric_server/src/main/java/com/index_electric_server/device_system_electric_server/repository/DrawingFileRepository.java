package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.DrawingFile;
import com.index_electric_server.device_system_electric_server.enums.DrawingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawingFileRepository extends JpaRepository<DrawingFile, Long> {
    // idx_drawing_object
    List<DrawingFile> findByObjectTypeAndObjectId(String objectType, Long objectId);
    List<DrawingFile> findByObjectTypeAndObjectIdAndDrawingType(String objectType, Long objectId, DrawingType drawingType);
    Optional<DrawingFile> findByDrawingCode(String drawingCode);

    // Load drawing + all panel coords + panels in 1 query — core DrawingViewer query
    @Query("""
        SELECT DISTINCT df FROM DrawingFile df
        JOIN FETCH df.panelCoords dpc
        JOIN FETCH dpc.panel ep
        WHERE df.drawingId = :drawingId
        ORDER BY dpc.xPercent
        """)
    Optional<DrawingFile> findWithPanelCoords(@Param("drawingId") Long drawingId);
}
