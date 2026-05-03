package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.entity.ElectricalPanel;
import com.index_electric_server.device_system_electric_server.enums.Status;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.ElectricalPanelRepository;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/panels")
@RequiredArgsConstructor
public class ElectricalPanelController {

    private final ElectricalPanelRepository panelRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectricalPanel>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            panelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + id))));
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<ApiResponse<List<ElectricalPanel>>> findByBuilding(
            @PathVariable Long buildingId,
            @RequestParam(required = false) Status status) {
        List<ElectricalPanel> list = status != null
            ? panelRepository.findByBuilding_BuildingIdAndStatus(buildingId, status)
            : panelRepository.findAll().stream()
                .filter(p -> p.getBuilding().getBuildingId().equals(buildingId))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    /**
     * Panel tree — root panels (no parent) with children.
     * GET /api/panels/tree/{buildingId}
     */
    @GetMapping("/tree/{buildingId}")
    public ResponseEntity<ApiResponse<List<ElectricalPanel>>> findTree(@PathVariable Long buildingId) {
        List<ElectricalPanel> roots = panelRepository
            .findByParentPanelIsNullAndBuilding_BuildingId(buildingId);
        return ResponseEntity.ok(ApiResponse.ok(roots));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ElectricalPanel>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(
            panelRepository.findByPanelCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + code))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ElectricalPanel>> create(@RequestBody ElectricalPanel panel) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(panelRepository.save(panel)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectricalPanel>> update(
            @PathVariable Long id, @RequestBody ElectricalPanel body) {
        ElectricalPanel panel = panelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + id));
        panel.setPanelName(body.getPanelName());
        panel.setPanelType(body.getPanelType());
        panel.setRatedCurrentA(body.getRatedCurrentA());
        panel.setRatedVoltage(body.getRatedVoltage());
        panel.setFloorLabel(body.getFloorLabel());
        panel.setAreaServed(body.getAreaServed());
        panel.setStatus(body.getStatus());
        panel.setNotes(body.getNotes());
        return ResponseEntity.ok(ApiResponse.ok("Updated", panelRepository.save(panel)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!panelRepository.existsById(id))
            throw new ResourceNotFoundException("Panel not found: " + id);
        panelRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
