package com.index_electric_server.reading.controller;
import com.index_electric_server.reading.entity.CompanyContact;
import com.index_electric_server.reading.service.companies.CompanyContactService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-contacts")
public class CompanyContactController {

    private final CompanyContactService contactService;

    public CompanyContactController(CompanyContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<CompanyContact> getAll() {
        return contactService.getAll();
    }

    @GetMapping("/{id}")
    public CompanyContact getById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<CompanyContact> getByCompany(@PathVariable Long companyId) {
        return contactService.getByCompanyId(companyId);
    }

    @PostMapping
    public CompanyContact create(@RequestBody CompanyContact contact) {
        return contactService.create(contact);
    }

    @PutMapping("/{id}")
    public CompanyContact update(
            @PathVariable Long id,
            @RequestBody CompanyContact contact
    ) {
        return contactService.update(id, contact);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        contactService.delete(id);
        return "Deleted contact id: " + id;
    }
}
