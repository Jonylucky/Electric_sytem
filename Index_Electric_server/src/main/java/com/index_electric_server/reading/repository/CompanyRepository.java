package com.index_electric_server.reading.repository;

import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import com.index_electric_server.reading.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}