package com.index_electric_server.device_system_electric_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class SiteDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String siteCode;
        private String siteName;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long siteId;
        private String siteCode;
        private String siteName;
        private String address;
        private LocalDateTime createdAt;
    }
}