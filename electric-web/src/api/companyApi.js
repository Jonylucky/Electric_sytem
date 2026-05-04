import apiClient from "./apiClient";

export function getCompanies() {
  return apiClient.get("/companies");
}

export function getCompanyById(companyId) {
  return apiClient.get(`/companies/${companyId}`);
}

export function createCompany(data) {
  return apiClient.post("/companies", data);
}

export function updateCompany(companyId, data) {
  return apiClient.put(`/companies/${companyId}`, data);
}

export function deleteCompany(companyId) {
  return apiClient.delete(`/companies/${companyId}`);
}

// Company Contacts API
export function getCompanyContacts() {
  return apiClient.get("/v1/company-contacts");
}

export function createCompanyContact(data) {
  return apiClient.post("/v1/company-contacts", data);
}

export function updateCompanyContact(contactId, data) {
  return apiClient.put(`/v1/company-contacts/${contactId}`, data);
}

export function deleteCompanyContact(contactId) {
  return apiClient.delete(`/v1/company-contacts/${contactId}`);
}

