package com.index_electric_server.reading.dto.metter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MeterRequest {

    @NotBlank(message = "meterId không được để trống")
    private String meterId;

    @NotNull(message = "companyId không được để trống")
    private Long companyId;

    @NotBlank(message = "meterName không được để trống")
    private String meterName;

    @NotNull(message = "locationId không được để trống")
    private Long locationId;

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
}