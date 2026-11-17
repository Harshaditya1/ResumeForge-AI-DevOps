import apiClient, { getCached } from "./apiClient";

/**
 * Save Job Description
 */
export const saveJobDescription = async (content) => {
  const response = await apiClient.post(
    "/api/job-descriptions",
    {
      content,
    }
  );

  return response.data;
};

/**
 * Get all Job Descriptions
 */
export const getJobDescriptions = async () => {
  return getCached("/api/job-descriptions");
};

/**
 * Get latest Job Description
 */
export const getLatestJobDescription = async () => {
  return getCached("/api/job-descriptions/latest");
};

/**
 * Get Job Description by ID
 */
export const getJobDescriptionById = async (
  jobDescriptionId
) => {
  return getCached(`/api/job-descriptions/${jobDescriptionId}`);
};

/**
 * Delete Job Description
 */
export const deleteJobDescription = async (
  jobDescriptionId
) => {
  const response = await apiClient.delete(
    `/api/job-descriptions/${jobDescriptionId}`
  );

  return response.data;
};