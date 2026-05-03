package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "site_code", nullable = false, unique = true, length = 50)
    private String siteCode;

    @Column(name = "site_name", nullable = false, length = 255)
    private String siteName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private List<Building> buildings;
}
