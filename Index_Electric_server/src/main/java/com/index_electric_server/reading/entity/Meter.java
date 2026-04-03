package com.index_electric_server.reading.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "meters",
        indexes = {
                @Index(name = "ix_meter_company", columnList = "company_id"),
                @Index(name = "ix_meter_location", columnList = "location_id")
        }
)
public class Meter {

    @Id
    @Column(name = "meter_id", length = 64)
    private String meterId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "meter_name")
    private String meterName;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Meter() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}