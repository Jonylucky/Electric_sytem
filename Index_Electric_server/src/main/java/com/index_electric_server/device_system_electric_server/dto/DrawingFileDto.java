package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.DrawingType;
import com.index_electric_server.device_system_electric_server.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DrawingFileDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String objectType;
        private Long objectId;
        private DrawingType drawingType;
        private String fileUrl;
        private String imageUrl;
        private String drawingCode;
        private String drawingLabel;
        private String revision;
        private LocalDate issuedDate;
        private Integer imageWPx;
        private Integer imageHPx;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long drawingId;
        private String objectType;
        private Long objectId;
        private DrawingType drawingType;
        private String fileUrl;
        private String imageUrl;
        private String drawingCode;
        private String drawingLabel;
        private String revision;
        private LocalDate issuedDate;
        private Integer imageWPx;
        private Integer imageHPx;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewerResponse {
        private Long drawingId;
        private String imageUrl;
        private String drawingLabel;
        private String drawingCode;
        private String revision;
        private Integer imageWPx;
        private Integer imageHPx;
        private List<PanelMarker> panels;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PanelMarker {
            private Long panelId;
            private String panelCode;
            private String panelName;
            private String panelType;
            private String floorLabel;
            private BigDecimal ratedCurrentA;
            private Status status;
            private BigDecimal xPercent;
            private BigDecimal yPercent;
            private String labelAnchor;
            private String extractMethod;
        }
    }
}