package com.index_electric_server.reading.service.companies;

import com.index_electric_server.reading.entity.Company;
import com.index_electric_server.reading.entity.CompanyContact;
import com.index_electric_server.reading.repository.CompanyContactRepository;
import com.index_electric_server.reading.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyContactService {

    private final CompanyContactRepository contactRepository;
    private final CompanyRepository companyRepository;

    public CompanyContactService(
            CompanyContactRepository contactRepository,
            CompanyRepository companyRepository
    ) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
    }

    public List<CompanyContact> getAll() {
        return contactRepository.findAll();
    }

    public CompanyContact getById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found: " + id));
    }

    public List<CompanyContact> getByCompanyId(Long companyId) {
        return contactRepository.findByCompany_CompanyId(companyId);
    }

    public CompanyContact create(CompanyContact contact) {
        Long companyId = contact.getCompany().getCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        contact.setCompany(company);

        return contactRepository.save(contact);
    }

    public CompanyContact update(Long id, CompanyContact newData) {
        CompanyContact contact = getById(id);

        contact.setContactName(newData.getContactName());
        contact.setEmail(newData.getEmail());
        contact.setPhone(newData.getPhone());
        contact.setContactType(newData.getContactType());
        contact.setIsPrimary(newData.getIsPrimary());
        contact.setIsActive(newData.getIsActive());

        if (newData.getCompany() != null && newData.getCompany().getCompanyId() != null) {
            Long companyId = newData.getCompany().getCompanyId();

            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

            contact.setCompany(company);
        }

        return contactRepository.save(contact);
    }

    public void delete(Long id) {
        CompanyContact contact = getById(id);
        contactRepository.delete(contact);
    }
}