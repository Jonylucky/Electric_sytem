package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.dto.DrawingFileDto;
import com.index_electric_server.device_system_electric_server.dto.DrawingPanelCoordDto;
import com.index_electric_server.device_system_electric_server.enums.DrawingType;
import com.index_electric_server.device_system_electric_server.service.DrawingService;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import com.index_electric_server.device_system_electric_server.util.CoordExtractUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drawings")
@RequiredArgsConstructor
public class DrawingController {

    private final DrawingService drawingService;

    @PostMapping
    public ResponseEntity<ApiResponse<DrawingFileDto.Response>> save(
            @RequestBody DrawingFileDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(drawingService.saveDrawing(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DrawingFileDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(drawingService.findById(id)));
    }

    /**
     * Core DrawingViewer endpoint — returns drawing image + all panel markers.
     * GET /api/drawings/1/viewer
     * Used by React DrawingViewer component.
     */
    @GetMapping("/{id}/viewer")
    public ResponseEntity<ApiResponse<DrawingFileDto.ViewerResponse>> loadViewer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(drawingService.loadViewerData(id)));
    }

    @GetMapping("/object")
    public ResponseEntity<ApiResponse<List<DrawingFileDto.Response>>> findByObject(
            @RequestParam String objectType,
            @RequestParam Long objectId,
            @RequestParam(required = false) DrawingType drawingType) {
        List<DrawingFileDto.Response> result = drawingType != null
            ? drawingService.findByObjectAndType(objectType, objectId, drawingType)
            : drawingService.findByObject(objectType, objectId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        drawingService.deleteDrawing(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ── Coord management ──────────────────────────────────────

    /**
     * Upsert a single panel marker coordinate.
     * Called when admin drags a marker in the UI.
     * PUT /api/drawings/coords
     */
    @PutMapping("/coords")
    public ResponseEntity<ApiResponse<DrawingPanelCoordDto.Response>> upsertCoord(
            @RequestBody DrawingPanelCoordDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Coordinate updated",
            drawingService.upsertCoord(request)));
    }

    /**
     * Batch upsert coordinates after PDF extraction by pdfplumber.
     * POST /api/drawings/coords/batch
     * Body: list of {drawing_id, panel_id, x_pts, y_pts, page_width, page_height}
     */
    @PostMapping("/coords/batch")
    public ResponseEntity<ApiResponse<List<DrawingPanelCoordDto.Response>>> batchFromPdf(
            @RequestBody List<Map<String, Object>> rawCoords) {
        List<DrawingPanelCoordDto.Request> requests = CoordExtractUtil.fromRawList(rawCoords);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(drawingService.batchUpsertCoords(requests)));
    }

    /**
     * Batch upsert pre-processed coordinates (already in percent).
     * POST /api/drawings/coords/batch-percent
     */
    @PostMapping("/coords/batch-percent")
    public ResponseEntity<ApiResponse<List<DrawingPanelCoordDto.Response>>> batchPercent(
            @RequestBody List<DrawingPanelCoordDto.Request> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(drawingService.batchUpsertCoords(requests)));
    }
}
