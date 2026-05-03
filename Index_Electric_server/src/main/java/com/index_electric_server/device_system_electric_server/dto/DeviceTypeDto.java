package com.index_electric_server.device_system_electric_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DeviceTypeDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String typeCode;
        private String typeName;
        private String category;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long deviceTypeId;
        private String typeCode;
        private String typeName;
        private String category;
        private String description;
    }
}