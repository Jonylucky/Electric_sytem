import apiClient from "./apiClient";

export function getReadingsByMonth(params) {
  return apiClient.get("v1/readings/month", { params });
}

export function getReadingsByMeter(meterId) {
  return apiClient.get(`/readings/meter/${meterId}`);
}

export function getChartConsumption(meterId, month) {
  return apiClient.get("/chart/consumption", { params: { meterId, month } });
}

export function getAllAlerts(month) {
  return apiClient.get("/alerts/all", { params: { month } });
}

export function createReading(data) {
  return apiClient.post("/readings", data);
}

export function updateReading(readingId, data) {
  return apiClient.put(`/readings/${readingId}`, data);
}

export function deleteReading(readingId) {
  return apiClient.delete(`/readings/${readingId}`);
}

