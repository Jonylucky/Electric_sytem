package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaintenanceRecordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private ObjectType objectType;
        private Long objectId;
        private String maintenanceCode;
        private String maintenanceType;
        private LocalDate scheduledDate;
        private LocalDate performedDate;
        private String performedBy;
        private String vendorName;
        private String resultStatus;
        private String findings;
        private String actionTaken;
        private LocalDate nextDueDate;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long maintenanceId;
        private ObjectType objectType;
        private Long objectId;
        private String maintenanceCode;
        private String maintenanceType;
        private LocalDate scheduledDate;
        private LocalDate performedDate;
        private String performedBy;
        private String vendorName;
        private String resultStatus;
        private String findings;
        private String actionTaken;
        private LocalDate nextDueDate;
        private String notes;
        private LocalDateTime createdAt;
    }
}