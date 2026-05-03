package com.index_electric_server.device_system_electric_server.dto;

import com.index_electric_server.device_system_electric_server.enums.PhotoObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AttachmentPhotoDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private PhotoObjectType objectType;
        private Long objectId;
        private String photoUrl;
        private String caption;
        private LocalDateTime takenAt;
        private String uploadedBy;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long photoId;
        private PhotoObjectType objectType;
        private Long objectId;
        private String photoUrl;
        private String caption;
        private LocalDateTime takenAt;
        private String uploadedBy;
        private Integer sortOrder;
        private LocalDateTime createdAt;
    }
}