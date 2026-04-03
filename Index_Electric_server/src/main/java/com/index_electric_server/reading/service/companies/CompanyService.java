package com.index_electric_server.reading.service.companies;

import com.index_electric_server.reading.dto.company.CompanyRequest;
import com.index_electric_server.reading.entity.Company;
import com.index_electric_server.reading.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
    }

    public Company createCompany(CompanyRequest req) {
        Company company = new Company();
        company.setCompanyName(req.getCompanyName());

        return companyRepository.save(company);
    }

    public Company updateCompany(Long companyId, CompanyRequest req) {
        Company old = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        old.setCompanyName(req.getCompanyName());

        return companyRepository.save(old);
    }

    public void deleteCompany(Long companyId) {
        Company old = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        companyRepository.delete(old);
    }
}