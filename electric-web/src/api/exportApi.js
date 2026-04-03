import apiClient from "./apiClient";

export function exportCompanyReportZip(params) {
  return apiClient.get("/export/company-report-zip", {
    params,
    responseType: "blob",
  });
}

export function exportCompanyReportPdf(params) {
  return apiClient.get("/export/company-report-pdf", {
    params,
    responseType: "blob",
  });
}

export function exportCompanyReportExcel(params) {
  return apiClient.get("/export/company-report-excel", {
    params,
    responseType: "blob",
  });
}

