import apiClient from "./apiClient";

export function getMeters(params) {
  return apiClient.get("/meters", { params });
}

export function getMeterById(meterId) {
  return apiClient.get(`/meters/${meterId}`);
}

export function createMeter(data) {
  return apiClient.post("/meters", data);
}

export function updateMeter(meterId, data) {
  return apiClient.put(`/meters/${meterId}`, data);
}

export function deleteMeter(meterId) {
  return apiClient.delete(`/meters/${meterId}`);
}

