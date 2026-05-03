package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EquipmentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long buildingId;
        private Long panelId;
        private Long deviceTypeId;
        private String equipmentCode;
        private String equipmentName;
        private String equipmentGroup;
        private String manufacturer;
        private String model;
        private String serialNumber;
        private BigDecimal ratedPowerKw;
        private BigDecimal ratedCurrentA;
        private String ratedVoltage;
        private String locationText;
        private Status status;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long equipmentId;
        private String equipmentCode;
        private String equipmentName;
        private String equipmentGroup;
        private Long deviceTypeId;
        private String deviceTypeName;
        private String deviceTypeCode;
        private Long buildingId;
        private String buildingName;
        private Long panelId;
        private String panelCode;
        private String manufacturer;
        private String model;
        private String serialNumber;
        private BigDecimal ratedPowerKw;
        private BigDecimal ratedCurrentA;
        private String ratedVoltage;
        private String locationText;
        private Status status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QrScanResponse {
        private Long equipmentId;
        private String equipmentCode;
        private String equipmentName;
        private Long deviceTypeId;
        private String deviceTypeName;
        private String locationText;
        private BigDecimal ratedCurrentA;
        private Status status;
        private List<DeviceCheckDefinitionDto.Response> checkDefinitions;
    }
}