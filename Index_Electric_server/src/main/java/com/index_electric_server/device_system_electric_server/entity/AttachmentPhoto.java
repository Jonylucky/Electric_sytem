package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.PhotoObjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachment_photos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttachmentPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private PhotoObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "photo_url", nullable = false, length = 1000)
    private String photoUrl;

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "uploaded_by", length = 255)
    private String uploadedBy;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
