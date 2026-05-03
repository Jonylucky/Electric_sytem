package com.index_electric_server.device_system_electric_server.service.impl;

import com.index_electric_server.device_system_electric_server.dto.DeviceCheckDefinitionDto;
import com.index_electric_server.device_system_electric_server.dto.EquipmentDto;
import com.index_electric_server.device_system_electric_server.entity.*;
import com.index_electric_server.device_system_electric_server.entity.*;
import com.index_electric_server.device_system_electric_server.enums.Status;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.*;
import com.index_electric_server.device_system_electric_server.repository.*;
import com.index_electric_server.device_system_electric_server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final BuildingRepository buildingRepository;
    private final ElectricalPanelRepository panelRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceCheckDefinitionRepository definitionRepository;

    @Override
    @Transactional
    public EquipmentDto.Response create(EquipmentDto.Request req) {
        Building building = buildingRepository.findById(req.getBuildingId())
            .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + req.getBuildingId()));
        DeviceType deviceType = deviceTypeRepository.findById(req.getDeviceTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("DeviceType not found: " + req.getDeviceTypeId()));
        ElectricalPanel panel = req.getPanelId() != null
            ? panelRepository.findById(req.getPanelId())
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + req.getPanelId()))
            : null;

        Equipment equipment = Equipment.builder()
            .building(building)
            .panel(panel)
            .deviceType(deviceType)
            .equipmentCode(req.getEquipmentCode())
            .equipmentName(req.getEquipmentName())
            .equipmentGroup(req.getEquipmentGroup())
            .manufacturer(req.getManufacturer())
            .model(req.getModel())
            .serialNumber(req.getSerialNumber())
            .ratedPowerKw(req.getRatedPowerKw())
            .ratedCurrentA(req.getRatedCurrentA())
            .ratedVoltage(req.getRatedVoltage())
            .locationText(req.getLocationText())
            .status(req.getStatus() != null ? req.getStatus() : Status.active)
            .notes(req.getNotes())
            .build();

        return toResponse(equipmentRepository.save(equipment));
    }

    @Override
    public EquipmentDto.Response findById(Long id) {
        return toResponse(equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + id)));
    }

    @Override
    public EquipmentDto.QrScanResponse findByQrCode(String equipmentCode) {
        // 1 query: equipment + device type + check definitions — no N+1
        Equipment eq = equipmentRepository.findWithCheckDefinitionsByCode(equipmentCode)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + equipmentCode));

        List<DeviceCheckDefinitionDto.Response> definitions =
            definitionRepository.findByDeviceType_DeviceTypeIdOrderBySortOrder(eq.getDeviceType().getDeviceTypeId())
                .stream()
                .map(this::toDefinitionResponse)
                .collect(Collectors.toList());

        return EquipmentDto.QrScanResponse.builder()
            .equipmentId(eq.getEquipmentId())
            .equipmentCode(eq.getEquipmentCode())
            .equipmentName(eq.getEquipmentName())
            .deviceTypeId(eq.getDeviceType().getDeviceTypeId())
            .deviceTypeName(eq.getDeviceType().getTypeName())
            .locationText(eq.getLocationText())
            .ratedCurrentA(eq.getRatedCurrentA())
            .status(eq.getStatus())
            .checkDefinitions(definitions)
            .build();
    }

    @Override
    public List<EquipmentDto.Response> findByBuilding(Long buildingId, Status status) {
        List<Equipment> list = status != null
            ? equipmentRepository.findByBuilding_BuildingIdAndStatus(buildingId, status)
            : equipmentRepository.findAll().stream()
                .filter(e -> e.getBuilding().getBuildingId().equals(buildingId))
                .collect(Collectors.toList());
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipmentDto.Response update(Long id, EquipmentDto.Request req) {
        Equipment eq = equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + id));
        eq.setEquipmentName(req.getEquipmentName());
        eq.setEquipmentGroup(req.getEquipmentGroup());
        eq.setManufacturer(req.getManufacturer());
        eq.setModel(req.getModel());
        eq.setSerialNumber(req.getSerialNumber());
        eq.setRatedPowerKw(req.getRatedPowerKw());
        eq.setRatedCurrentA(req.getRatedCurrentA());
        eq.setRatedVoltage(req.getRatedVoltage());
        eq.setLocationText(req.getLocationText());
        if (req.getStatus() != null) eq.setStatus(req.getStatus());
        eq.setNotes(req.getNotes());
        return toResponse(equipmentRepository.save(eq));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment not found: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    private EquipmentDto.Response toResponse(Equipment e) {
        return EquipmentDto.Response.builder()
            .equipmentId(e.getEquipmentId())
            .equipmentCode(e.getEquipmentCode())
            .equipmentName(e.getEquipmentName())
            .equipmentGroup(e.getEquipmentGroup())
            .deviceTypeId(e.getDeviceType().getDeviceTypeId())
            .deviceTypeName(e.getDeviceType().getTypeName())
            .deviceTypeCode(e.getDeviceType().getTypeCode())
            .buildingId(e.getBuilding().getBuildingId())
            .buildingName(e.getBuilding().getBuildingName())
            .panelId(e.getPanel() != null ? e.getPanel().getPanelId() : null)
            .panelCode(e.getPanel() != null ? e.getPanel().getPanelCode() : null)
            .manufacturer(e.getManufacturer())
            .model(e.getModel())
            .serialNumber(e.getSerialNumber())
            .ratedPowerKw(e.getRatedPowerKw())
            .ratedCurrentA(e.getRatedCurrentA())
            .ratedVoltage(e.getRatedVoltage())
            .locationText(e.getLocationText())
            .status(e.getStatus())
            .createdAt(e.getCreatedAt())
            .build();
    }

    private DeviceCheckDefinitionDto.Response toDefinitionResponse(DeviceCheckDefinition cd) {
        return DeviceCheckDefinitionDto.Response.builder()
            .checkDefinitionId(cd.getCheckDefinitionId())
            .deviceTypeId(cd.getDeviceType().getDeviceTypeId())
            .itemCode(cd.getItemCode())
            .itemName(cd.getItemName())
            .itemGroup(cd.getItemGroup())
            .checkInputType(cd.getCheckInputType())
            .unitName(cd.getUnitName())
            .minValue(cd.getMinValue())
            .maxValue(cd.getMaxValue())
            .sortOrder(cd.getSortOrder())
            .isRequired(cd.getIsRequired())
            .defaultXPercent(cd.getDefaultXPercent())
            .defaultYPercent(cd.getDefaultYPercent())
            .xPercent(cd.getDefaultXPercent())   // fallback; override with image marker if exists
            .yPercent(cd.getDefaultYPercent())
            .labelPosition("right")
            .build();
    }
}
