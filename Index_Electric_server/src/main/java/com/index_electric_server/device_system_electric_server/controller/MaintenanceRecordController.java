package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.entity.MaintenanceRecord;
import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.MaintenanceRecordRepository;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final MaintenanceRecordRepository maintenanceRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceRecord>> create(
            @RequestBody MaintenanceRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(maintenanceRepository.save(record)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found: " + id))));
    }

    /**
     * History per object — uses composite index (object_type, object_id, performed_date DESC).
     * GET /api/maintenance/object?type=equipment&id=55
     */
    @GetMapping("/object")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> findByObject(
            @RequestParam ObjectType type,
            @RequestParam Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            maintenanceRepository.findByObjectTypeAndObjectIdOrderByPerformedDateDesc(type, id)));
    }

    /**
     * Upcoming scheduled maintenance.
     * GET /api/maintenance/scheduled?from=2026-04-01&to=2026-04-30
     */
    @GetMapping("/scheduled")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> findScheduled(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(
            maintenanceRepository.findByScheduledDateBetween(from, to)));
    }

    /**
     * Upcoming due dates — for warranty/maintenance alert cron.
     * GET /api/maintenance/due?from=2026-04-01&to=2026-04-30
     */
    @GetMapping("/due")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> findDue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(
            maintenanceRepository.findByNextDueDateBetween(from, to)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> update(
            @PathVariable Long id, @RequestBody MaintenanceRecord body) {
        MaintenanceRecord record = maintenanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found: " + id));
        record.setMaintenanceType(body.getMaintenanceType());
        record.setScheduledDate(body.getScheduledDate());
        record.setPerformedDate(body.getPerformedDate());
        record.setPerformedBy(body.getPerformedBy());
        record.setVendorName(body.getVendorName());
        record.setResultStatus(body.getResultStatus());
        record.setFindings(body.getFindings());
        record.setActionTaken(body.getActionTaken());
        record.setNextDueDate(body.getNextDueDate());
        record.setNotes(body.getNotes());
        return ResponseEntity.ok(ApiResponse.ok("Updated", maintenanceRepository.save(record)));
    }
}
