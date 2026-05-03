package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DeviceErrorCodeDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long deviceTypeId;
        private String errorCode;
        private String errorName;
        private String errorCategory;
        private SeverityLevel severityLevel;
        private String description;
        private String possibleCause;
        private String operatorAction;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long errorCodeId;
        private Long deviceTypeId;
        private String typeCode;
        private String errorCode;
        private String errorName;
        private String errorCategory;
        private SeverityLevel severityLevel;
        private String possibleCause;
        private String operatorAction;
        private Boolean isActive;
    }
}