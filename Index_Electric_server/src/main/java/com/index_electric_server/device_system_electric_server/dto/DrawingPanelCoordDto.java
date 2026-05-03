package com.index_electric_server.device_system_electric_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DrawingPanelCoordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long drawingId;
        private Long panelId;
        private BigDecimal xPercent;
        private BigDecimal yPercent;
        private String labelAnchor;
        private String extractMethod;
        private BigDecimal confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long coordId;
        private Long drawingId;
        private Long panelId;
        private String panelCode;
        private String panelName;
        private BigDecimal xPercent;
        private BigDecimal yPercent;
        private String labelAnchor;
        private String extractMethod;
        private BigDecimal confidence;
        private LocalDateTime updatedAt;
    }
}