import apiClient, { getCached } from "./apiClient";

export const generateInterviewQuestions = async () => {
  const response = await apiClient.post("/api/interview-questions");
  return response.data;
};

export const getLatestInterviewQuestions = async () => {
  return getCached("/api/interview-questions/latest");
};

export const getInterviewHistory = async () => {
  return getCached("/api/interview-questions/history");
};