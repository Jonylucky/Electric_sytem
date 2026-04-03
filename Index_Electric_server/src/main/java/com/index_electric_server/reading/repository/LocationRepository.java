package com.index_electric_server.reading.repository;

import com.index_electric_server.reading.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}