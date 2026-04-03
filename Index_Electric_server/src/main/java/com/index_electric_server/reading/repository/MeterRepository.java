package com.index_electric_server.reading.repository;

import com.index_electric_server.reading.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, String> {


    List<Meter> findByCompanyIdOrderByMeterIdAsc(Long companyId);

    List<Meter> findByLocationIdOrderByMeterIdAsc(Long locationId);

    List<Meter> findByCompanyIdAndLocationIdOrderByMeterIdAsc(Long companyId, Long locationId);

    List<Meter> findByMeterIdIn(List<String> meterIds);

    boolean existsByMeterId(String meterId);

    List<Meter> findByCompanyId(Long companyId);

    List<Meter> findByLocationId(Long locationId);

}