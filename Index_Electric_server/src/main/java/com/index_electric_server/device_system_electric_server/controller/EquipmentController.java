package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.dto.EquipmentDto;
import com.index_electric_server.device_system_electric_server.enums.Status;
import com.index_electric_server.device_system_electric_server.service.EquipmentService;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentDto.Response>> create(
            @RequestBody EquipmentDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(equipmentService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(equipmentService.findById(id)));
    }

    /**
     * QR scan endpoint — called by Flutter when technician scans QR code.
     * Returns equipment info + full check definition list in 1 query.
     * GET /api/equipment/qr?code=EQ-00055
     */
    @GetMapping("/qr")
    public ResponseEntity<ApiResponse<EquipmentDto.QrScanResponse>> findByQrCode(
            @RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.ok(equipmentService.findByQrCode(code)));
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<ApiResponse<List<EquipmentDto.Response>>> findByBuilding(
            @PathVariable Long buildingId,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(ApiResponse.ok(equipmentService.findByBuilding(buildingId, status)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto.Response>> update(
            @PathVariable Long id,
            @RequestBody EquipmentDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", equipmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
