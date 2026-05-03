package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.entity.DeviceType;
import com.index_electric_server.device_system_electric_server.entity.DeviceCheckDefinition;
import com.index_electric_server.device_system_electric_server.entity.DeviceErrorCode;
import com.index_electric_server.device_system_electric_server.enums.SeverityLevel;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.*;
import com.index_electric_server.device_system_electric_server.repository.DeviceCheckDefinitionRepository;
import com.index_electric_server.device_system_electric_server.repository.DeviceErrorCodeRepository;
import com.index_electric_server.device_system_electric_server.repository.DeviceTypeRepository;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-types")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceCheckDefinitionRepository definitionRepository;
    private final DeviceErrorCodeRepository errorCodeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceType>>> findAll(
            @RequestParam(required = false) String category) {
        List<DeviceType> list = category != null
            ? deviceTypeRepository.findByCategory(category)
            : deviceTypeRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceType>> findById(@PathVariable Long id) {
        DeviceType dt = deviceTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("DeviceType not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(dt));
    }

    /**
     * Load check definitions for a device type — sorted by sort_order.
     * Used by Flutter app when loading inspection form.
     * GET /api/device-types/11/definitions
     */
    @GetMapping("/{id}/definitions")
    public ResponseEntity<ApiResponse<List<DeviceCheckDefinition>>> findDefinitions(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            definitionRepository.findByDeviceType_DeviceTypeIdOrderBySortOrder(id)));
    }

    /**
     * Load error codes for a device type — for dropdown/lookup on mobile.
     * GET /api/device-types/11/error-codes?severity=critical
     */
    @GetMapping("/{id}/error-codes")
    public ResponseEntity<ApiResponse<List<DeviceErrorCode>>> findErrorCodes(
            @PathVariable Long id,
            @RequestParam(required = false) SeverityLevel severity) {
        List<DeviceErrorCode> list = severity != null
            ? errorCodeRepository.findByDeviceType_DeviceTypeIdAndSeverityLevel(id, severity)
            : errorCodeRepository
                .findByDeviceType_DeviceTypeIdAndIsActiveTrueOrderBySeverityLevelDescErrorCodeAsc(id);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceType>> create(@RequestBody DeviceType deviceType) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(deviceTypeRepository.save(deviceType)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceType>> update(
            @PathVariable Long id, @RequestBody DeviceType body) {
        DeviceType dt = deviceTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("DeviceType not found: " + id));
        dt.setTypeName(body.getTypeName());
        dt.setCategory(body.getCategory());
        dt.setDescription(body.getDescription());
        return ResponseEntity.ok(ApiResponse.ok("Updated", deviceTypeRepository.save(dt)));
    }
}
