package com.index_electric_server.reading.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_location_name_floor", columnNames = {"location_name","floor"})
        }
)
public class Location {

    @Id
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    public Location() {}

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }
}