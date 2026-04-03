package com.index_electric_server.reading.repository;

import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import com.index_electric_server.reading.entity.Reading;
import com.index_electric_server.reading.repository.ReadingRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    Optional<Reading> findByMeterIdAndMonthAndReadingType(String meterId, String month, Reading.ReadingType readingType);
    @Query("""
    select r from Reading r
    where r.readingType = com.index_electric_server.reading.entity.Reading.ReadingType.OFFICIAL
      and r.imageKey = :imageKey
  """)
    Optional<Reading> findOfficialByImageKey(@Param("imageKey") String imageKey);

    @Query("select r.indexLastMonth from Reading r " +
            "where r.meterId = :meterId and r.month = :month and r.readingType = 'OFFICIAL'")
    Optional<Integer> findOfficialIndexLastMonth(@Param("meterId") String meterId, @Param("month") String month);

    // Validate meter tồn tại: query trực tiếp bảng meters
    @Query(value = "select case when count(1) > 0 then true else false end from meters m where m.meter_id = :meterId",
            nativeQuery = true)
    boolean existsMeter(@Param("meterId") String meterId);
    // ✅ update imageUrl theo readingId (nhanh, không cần load entity)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Reading r set r.imageUrl = :url where r.readingId = :id")
    int updateImageUrl(@Param("id") Long readingId, @Param("url") String url);

    List<Reading> findByMonthAndReadingTypeOrderByMeterIdAsc(
            String month,
            Reading.ReadingType readingType
    );

    //report
    @Query("""
select new com.index_electric_server.reading.dto.report.ExportReadingRowDto(
    r.meterId,
    m.meterName,
    c.companyName,
    r.month,
    r.indexPrevMonth,
    r.indexLastMonth,
    r.indexConsumption,
    l.locationName,
    r.imageUrl,
    r.alertLevel
)
from Reading r
join Meter m on m.meterId = r.meterId
join Company c on c.companyId = m.companyId
left join Location l on l.locationId = m.locationId
where r.month = :month
order by c.companyName asc, r.meterId asc
""")
    List<ExportReadingRowDto> findExportRowsByMonth(String month);

    @Query("""
select new com.index_electric_server.reading.dto.report.ExportReadingRowDto(
    r.meterId,
    m.meterName,
    c.companyName,
    r.month,
    r.indexPrevMonth,
    r.indexLastMonth,
    r.indexConsumption,
    l.locationName,
    r.imageUrl,
    r.alertLevel
)
from Reading r
join Meter m on m.meterId = r.meterId
join Company c on c.companyId = m.companyId
left join Location l on l.locationId = m.locationId
where r.month = :month
  and c.companyId = :companyId
order by l.locationName asc, r.meterId asc
""")
    List<ExportReadingRowDto> findExportRowsByMonthAndCompanyId(String month, Long companyId);
    @Query(value = """
    SELECT
        r.reading_id AS readingId,
        c.company_id AS companyId,
        c.company_name AS companyName,
        m.meter_id AS meterId,
        m.meter_name AS meterName,
        r.month AS month,
        r.index_consumption AS indexConsumption,
        r.alert_level AS alertLevel,
        r.image_url AS imageUrl
    FROM readings r
    JOIN meters m ON r.meter_id = m.meter_id
    JOIN companies c ON m.company_id = c.company_id
    WHERE r.month = :month
      AND r.reading_type = 'OFFICIAL'
      AND r.alert_level IN ('WARNING','DANGER')
    ORDER BY
        CASE r.alert_level
            WHEN 'DANGER' THEN 1
            WHEN 'WARNING' THEN 2
            ELSE 3
        END,
        c.company_id,
        m.meter_id
    """, nativeQuery = true)
    List<AlertReadingProjection> findAllAlertsByMonth(@Param("month") String month);
    @Query(value = """
    SELECT
        r.meter_id AS meterId,
        r.month AS month,
        r.index_prev_month AS indexPrevMonth,
        r.index_last_month AS indexLastMonth,
        r.index_consumption AS indexConsumption,
        r.alert_level AS alertLevel
    FROM readings r
    WHERE r.meter_id = :meterId
      AND r.reading_type = 'OFFICIAL'
      AND r.month IN (:months)
    ORDER BY r.month ASC
    """, nativeQuery = true)
    List<ReadingChartProjection> findChartByMeterIdAndMonths(
            @Param("meterId") String meterId,
            @Param("months") List<String> months
    );
}