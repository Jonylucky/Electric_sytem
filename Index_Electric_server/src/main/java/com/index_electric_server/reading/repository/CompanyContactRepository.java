package com.index_electric_server.reading.repository;

import com.index_electric_server.reading.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {

    List<CompanyContact> findByCompanyCompanyId(Long companyId);

    List<CompanyContact> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);

    List<CompanyContact> findByIsActiveTrue();

    Optional<CompanyContact> findByCompanyCompanyIdAndEmail(Long companyId, String email);

    boolean existsByCompanyCompanyIdAndEmail(Long companyId, String email);

    List<CompanyContact> findByContactTypeAndIsActiveTrue(String contactType);
}