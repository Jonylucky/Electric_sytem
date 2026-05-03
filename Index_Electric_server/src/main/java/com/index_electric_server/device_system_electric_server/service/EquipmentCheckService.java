package com.index_electric_server.device_system_electric_server.service;

import com.index_electric_server.device_system_electric_server.dto.EquipmentCheckDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentCheckService {
    EquipmentCheckDto.Response create(EquipmentCheckDto.Request request);
    EquipmentCheckDto.Response findById(Long id);
    List<EquipmentCheckDto.Response> findByEquipment(Long equipmentId);
    List<EquipmentCheckDto.Response> findByEquipmentAndDateRange(Long equipmentId, LocalDateTime from, LocalDateTime to);
    EquipmentCheckDto.SyncResponse syncFromMobile(EquipmentCheckDto.SyncRequest request);
    List<EquipmentCheckDto.Response.ItemResponse> findOutOfRange(LocalDateTime from);
}
