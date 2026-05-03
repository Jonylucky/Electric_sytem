package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ElectricalPanelDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long buildingId;
        private Long roomId;
        private Long parentPanelId;
        private String sourceType;
        private Long sourceId;
        private String panelCode;
        private String panelName;
        private String panelType;
        private BigDecimal ratedCurrentA;
        private String ratedVoltage;
        private String floorLabel;
        private String areaServed;
        private Status status;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long panelId;
        private Long buildingId;
        private String buildingName;
        private Long parentPanelId;
        private String parentPanelName;
        private String panelCode;
        private String panelName;
        private String panelType;
        private BigDecimal ratedCurrentA;
        private String ratedVoltage;
        private String floorLabel;
        private String areaServed;
        private Status status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode {
        private Long panelId;
        private String panelCode;
        private String panelName;
        private String panelType;
        private String floorLabel;
        private Status status;
        private List<TreeNode> children;
    }
}