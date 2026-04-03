package com.index_electric_server.reading.controller;

import com.index_electric_server.reading.dto.company.CompanyRequest;
import com.index_electric_server.reading.entity.Company;
import com.index_electric_server.reading.service.companies.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")

public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(company);
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@Valid @RequestBody CompanyRequest request) {
        Company saved = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<Company> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyRequest request
    ) {
        Company updated = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok("Delete company successfully: " + companyId);
    }
}