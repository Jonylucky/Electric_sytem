package com.index_electric_server.reading.repository;

public interface AlertReadingProjection {
    Long getReadingId();

    Long getCompanyId();

    String getCompanyName();

    String getMeterId();

    String getMeterName();

    String getMonth();

    Integer getIndexConsumption();

    String getAlertLevel();

    String getImageUrl();
}
