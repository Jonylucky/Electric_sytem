package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.dto.EquipmentCheckDto;
import com.index_electric_server.device_system_electric_server.service.EquipmentCheckService;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/checks")
@RequiredArgsConstructor
public class EquipmentCheckController {

    private final EquipmentCheckService checkService;

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentCheckDto.Response>> create(
            @RequestBody EquipmentCheckDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(checkService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentCheckDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(checkService.findById(id)));
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<ApiResponse<List<EquipmentCheckDto.Response>>> findByEquipment(
            @PathVariable Long equipmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<EquipmentCheckDto.Response> result = from != null
            ? checkService.findByEquipmentAndDateRange(equipmentId, from, to)
            : checkService.findByEquipment(equipmentId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Batch sync from Flutter offline app.
     * POST /api/checks/sync
     * Body: { "checks": [ { equipment_id, check_date, items: [...] } ] }
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<EquipmentCheckDto.SyncResponse>> syncFromMobile(
            @RequestBody EquipmentCheckDto.SyncRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(checkService.syncFromMobile(request)));
    }

    /**
     * Out-of-range items for dashboard alerting.
     * GET /api/checks/out-of-range?from=2026-03-01T00:00:00
     */
    @GetMapping("/out-of-range")
    public ResponseEntity<ApiResponse<List<EquipmentCheckDto.Response.ItemResponse>>> findOutOfRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from) {
        return ResponseEntity.ok(ApiResponse.ok(checkService.findOutOfRange(from)));
    }
}
