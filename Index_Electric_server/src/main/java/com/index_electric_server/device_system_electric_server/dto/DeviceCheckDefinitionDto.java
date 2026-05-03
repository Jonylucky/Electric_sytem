package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.CheckInputType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class DeviceCheckDefinitionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long deviceTypeId;
        private String itemCode;
        private String itemName;
        private String itemGroup;
        private CheckInputType checkInputType;
        private String unitName;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private Integer sortOrder;
        private Boolean isRequired;
        private BigDecimal defaultXPercent;
        private BigDecimal defaultYPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long checkDefinitionId;
        private Long deviceTypeId;
        private String itemCode;
        private String itemName;
        private String itemGroup;
        private CheckInputType checkInputType;
        private String unitName;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private Integer sortOrder;
        private Boolean isRequired;
        private BigDecimal defaultXPercent;
        private BigDecimal defaultYPercent;
        private BigDecimal xPercent;
        private BigDecimal yPercent;
        private String labelPosition;
    }
}