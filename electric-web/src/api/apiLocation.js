import apiClient from "./apiClient";

export function getLocations() {
  return apiClient.get("/v1/locations").then(response => response.data);
}
