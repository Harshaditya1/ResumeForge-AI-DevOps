import apiClient, { getCached } from "./apiClient";

/**
 * Analyze latest Resume against latest Job Description
 */
export const analyzeResume = async () => {
  const response = await apiClient.post(
    "/api/analysis"
  );

  return response.data;
};

/**
 * Get latest analysis
 */
export const getLatestAnalysis = async () => {
  return getCached("/api/analysis/latest");
};

/**
 * Get analysis history
 */
export const getAnalysisHistory = async () => {
  return getCached("/api/analysis/history");
};