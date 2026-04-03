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

