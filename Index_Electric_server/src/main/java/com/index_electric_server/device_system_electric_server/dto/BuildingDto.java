package com.index_electric_server.device_system_electric_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BuildingDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long siteId;
        private String buildingCode;
        private String buildingName;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long buildingId;
        private Long siteId;
        private String siteName;
        private String buildingCode;
        private String buildingName;
        private String description;
        private LocalDateTime createdAt;
    }
}