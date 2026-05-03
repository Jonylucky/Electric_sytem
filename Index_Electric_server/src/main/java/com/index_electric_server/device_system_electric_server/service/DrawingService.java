package com.index_electric_server.device_system_electric_server.service;

import com.index_electric_server.device_system_electric_server.dto.DrawingFileDto;
import com.index_electric_server.device_system_electric_server.dto.DrawingPanelCoordDto;
import com.index_electric_server.device_system_electric_server.enums.DrawingType;

import java.util.List;

public interface DrawingService {
    DrawingFileDto.Response saveDrawing(DrawingFileDto.Request request);
    DrawingFileDto.Response findById(Long drawingId);
    List<DrawingFileDto.Response> findByObject(String objectType, Long objectId);
    List<DrawingFileDto.Response> findByObjectAndType(String objectType, Long objectId, DrawingType type);

    // Core DrawingViewer: load drawing + all panel markers in 1 query
    DrawingFileDto.ViewerResponse loadViewerData(Long drawingId);

    // Upsert a single marker coordinate
    DrawingPanelCoordDto.Response upsertCoord(DrawingPanelCoordDto.Request request);

    // Batch upsert after PDF extraction
    List<DrawingPanelCoordDto.Response> batchUpsertCoords(List<DrawingPanelCoordDto.Request> requests);

    void deleteDrawing(Long drawingId);
}
