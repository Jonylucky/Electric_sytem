package com.index_electric_server.device_system_electric_server.service.impl;

import com.index_electric_server.device_system_electric_server.dto.EquipmentCheckDto;
import com.index_electric_server.device_system_electric_server.entity.*;
import com.index_electric_server.device_system_electric_server.entity.DeviceCheckDefinition;
import com.index_electric_server.device_system_electric_server.entity.Equipment;
import com.index_electric_server.device_system_electric_server.entity.EquipmentCheck;
import com.index_electric_server.device_system_electric_server.entity.EquipmentCheckItem;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.*;
import com.index_electric_server.device_system_electric_server.repository.DeviceCheckDefinitionRepository;
import com.index_electric_server.device_system_electric_server.repository.EquipmentCheckItemRepository;
import com.index_electric_server.device_system_electric_server.repository.EquipmentCheckRepository;
import com.index_electric_server.device_system_electric_server.repository.EquipmentRepository;
import com.index_electric_server.device_system_electric_server.service.EquipmentCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentCheckServiceImpl implements EquipmentCheckService {

    private final EquipmentCheckRepository checkRepository;
    private final EquipmentCheckItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final DeviceCheckDefinitionRepository definitionRepository;

    @Override
    @Transactional
    public EquipmentCheckDto.Response create(EquipmentCheckDto.Request req) {
        Equipment equipment = equipmentRepository.findById(req.getEquipmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + req.getEquipmentId()));

        EquipmentCheck check = EquipmentCheck.builder()
            .equipment(equipment)
            .checkDate(req.getCheckDate() != null ? req.getCheckDate() : LocalDateTime.now())
            .checkedBy(req.getCheckedBy())
            .overallCondition(req.getOverallCondition())
            .remark(req.getRemark())
            .build();
        check = checkRepository.save(check);

        List<EquipmentCheckItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (EquipmentCheckDto.Request.ItemRequest ir : req.getItems()) {
                DeviceCheckDefinition def = definitionRepository.findById(ir.getCheckDefinitionId())
                    .orElseThrow(() -> new ResourceNotFoundException("CheckDefinition not found: " + ir.getCheckDefinitionId()));
                items.add(EquipmentCheckItem.builder()
                    .equipmentCheck(check)
                    .checkDefinition(def)
                    .statusCode(ir.getStatusCode())
                    .textValue(ir.getTextValue())
                    .numericValue(ir.getNumericValue())
                    .note(ir.getNote())
                    .build());
            }
            itemRepository.saveAll(items);
        }
        check.setItems(items);
        return toResponse(check);
    }

    @Override
    public EquipmentCheckDto.Response findById(Long id) {
        EquipmentCheck check = checkRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("EquipmentCheck not found: " + id));
        return toResponse(check);
    }

    @Override
    public List<EquipmentCheckDto.Response> findByEquipment(Long equipmentId) {
        return checkRepository.findByEquipment_EquipmentIdOrderByCheckDateDesc(equipmentId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<EquipmentCheckDto.Response> findByEquipmentAndDateRange(Long equipmentId, LocalDateTime from, LocalDateTime to) {
        return checkRepository.findWithItemsByEquipmentAndDateRange(equipmentId, from)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipmentCheckDto.SyncResponse syncFromMobile(EquipmentCheckDto.SyncRequest syncRequest) {
        List<EquipmentCheckDto.SyncResponse.SyncResult> synced = new ArrayList<>();
        List<EquipmentCheckDto.SyncResponse.SyncFailed> failed = new ArrayList<>();
        List<EquipmentCheckDto.SyncResponse.OutOfRangeWarning> warnings = new ArrayList<>();

        for (EquipmentCheckDto.Request req : syncRequest.getChecks()) {
            try {
                EquipmentCheckDto.Response resp = create(req);
                synced.add(EquipmentCheckDto.SyncResponse.SyncResult.builder()
                    .serverCheckId(resp.getCheckId())
                    .build());

                // collect out-of-range warnings
                if (resp.getItems() != null) {
                    resp.getItems().stream()
                        .filter(item -> Boolean.TRUE.equals(item.getOutOfRange()))
                        .forEach(item -> warnings.add(
                            EquipmentCheckDto.SyncResponse.OutOfRangeWarning.builder()
                                .checkDefinitionId(item.getCheckDefinitionId())
                                .itemName(item.getItemName())
                                .numericValue(item.getNumericValue())
                                .minValue(item.getMinValue())
                                .maxValue(item.getMaxValue())
                                .build()
                        ));
                }
            } catch (Exception ex) {
                failed.add(EquipmentCheckDto.SyncResponse.SyncFailed.builder()
                    .reason(ex.getMessage())
                    .build());
            }
        }
        return EquipmentCheckDto.SyncResponse.builder()
            .synced(synced).failed(failed).warnings(warnings)
            .build();
    }

    @Override
    public List<EquipmentCheckDto.Response.ItemResponse> findOutOfRange(LocalDateTime from) {
        return itemRepository.findOutOfRangeSince(from)
            .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    // ── Mappers ──────────────────────────────────────────────

    private EquipmentCheckDto.Response toResponse(EquipmentCheck c) {
        List<EquipmentCheckDto.Response.ItemResponse> items = c.getItems() == null ? List.of()
            : c.getItems().stream().map(this::toItemResponse).collect(Collectors.toList());
        return EquipmentCheckDto.Response.builder()
            .checkId(c.getCheckId())
            .equipmentId(c.getEquipment().getEquipmentId())
            .equipmentName(c.getEquipment().getEquipmentName())
            .checkDate(c.getCheckDate())
            .checkedBy(c.getCheckedBy())
            .overallCondition(c.getOverallCondition())
            .remark(c.getRemark())
            .items(items)
            .createdAt(c.getCreatedAt())
            .build();
    }

    private EquipmentCheckDto.Response.ItemResponse toItemResponse(EquipmentCheckItem ci) {
        DeviceCheckDefinition cd = ci.getCheckDefinition();
        boolean outOfRange = false;
        if (ci.getNumericValue() != null && cd.getMinValue() != null && cd.getMaxValue() != null) {
            outOfRange = ci.getNumericValue().compareTo(cd.getMinValue()) < 0
                      || ci.getNumericValue().compareTo(cd.getMaxValue()) > 0;
        }
        return EquipmentCheckDto.Response.ItemResponse.builder()
            .checkItemId(ci.getCheckItemId())
            .checkDefinitionId(cd.getCheckDefinitionId())
            .itemName(cd.getItemName())
            .itemGroup(cd.getItemGroup())
            .checkInputType(cd.getCheckInputType())
            .unitName(cd.getUnitName())
            .minValue(cd.getMinValue())
            .maxValue(cd.getMaxValue())
            .statusCode(ci.getStatusCode())
            .textValue(ci.getTextValue())
            .numericValue(ci.getNumericValue())
            .outOfRange(outOfRange)
            .note(ci.getNote())
            .xPercent(cd.getDefaultXPercent())
            .yPercent(cd.getDefaultYPercent())
            .build();
    }
}
