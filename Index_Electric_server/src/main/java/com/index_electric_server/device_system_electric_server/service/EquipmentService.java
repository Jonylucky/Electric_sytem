package com.index_electric_server.device_system_electric_server.service;

import com.index_electric_server.device_system_electric_server.dto.EquipmentDto;
import com.index_electric_server.device_system_electric_server.enums.Status;

import java.util.List;

public interface EquipmentService {
    EquipmentDto.Response create(EquipmentDto.Request request);
    EquipmentDto.Response findById(Long id);
    EquipmentDto.QrScanResponse findByQrCode(String equipmentCode);
    List<EquipmentDto.Response> findByBuilding(Long buildingId, Status status);
    EquipmentDto.Response update(Long id, EquipmentDto.Request request);
    void delete(Long id);
}
