package com.index_electric_server.reading.dto.location;

public class LocationDto {

    private Long locationId;
    private String locationName;
    private Integer floor;

    public LocationDto() {
    }

    public LocationDto(Long locationId, String locationName, Integer floor) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.floor = floor;
    }

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