package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.CheckInputType;
import com.index_electric_server.device_system_electric_server.enums.OverallCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EquipmentCheckDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long equipmentId;
        private LocalDateTime checkDate;
        private String checkedBy;
        private OverallCondition overallCondition;
        private String remark;
        private List<ItemRequest> items;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ItemRequest {
            private Long checkDefinitionId;
            private String statusCode;
            private String textValue;
            private BigDecimal numericValue;
            private String note;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long checkId;
        private Long equipmentId;
        private String equipmentName;
        private LocalDateTime checkDate;
        private String checkedBy;
        private OverallCondition overallCondition;
        private String remark;
        private List<ItemResponse> items;
        private LocalDateTime createdAt;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ItemResponse {
            private Long checkItemId;
            private Long checkDefinitionId;
            private String itemName;
            private String itemGroup;
            private CheckInputType checkInputType;
            private String unitName;
            private BigDecimal minValue;
            private BigDecimal maxValue;
            private String statusCode;
            private String textValue;
            private BigDecimal numericValue;
            private Boolean outOfRange;
            private String note;
            private BigDecimal xPercent;
            private BigDecimal yPercent;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncRequest {
        private List<Request> checks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResponse {
        private List<SyncResult> synced;
        private List<SyncFailed> failed;
        private List<OutOfRangeWarning> warnings;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SyncResult {
            private Long localCheckId;
            private Long serverCheckId;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SyncFailed {
            private Long localCheckId;
            private String reason;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OutOfRangeWarning {
            private Long checkDefinitionId;
            private String itemName;
            private BigDecimal numericValue;
            private BigDecimal minValue;
            private BigDecimal maxValue;
        }
    }
}