import apiClient, { getCached } from "./apiClient";

export const getApplications = () =>
  getCached("/api/applications");

export const getApplicationStatistics = () =>
  getCached("/api/applications/statistics");

export const createApplication = async (application) => {
  const response = await apiClient.post(
    "/api/applications",
    application
  );
  return response.data;
};

export const updateApplication = async (id, application) => {
  const response = await apiClient.put(
    `/api/applications/${id}`,
    application
  );
  return response.data;
};

export const deleteApplication = async (id) => {
  await apiClient.delete(`/api/applications/${id}`);
};
