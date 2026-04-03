package com.index_electric_server.reading.repository;

public interface ReadingChartProjection {

    String getMeterId();

    String getMonth();

    Integer getIndexPrevMonth();

    Integer getIndexLastMonth();

    Integer getIndexConsumption();

    String getAlertLevel();
}