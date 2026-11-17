import apiClient, { getCached } from "./apiClient";

export const getLatestResumeHealth = () =>
  getCached("/api/resume-health/latest");

export const getResumeHealth = (resumeId) =>
  getCached(`/api/resume-health/${resumeId}`);

export const updateResumeHealthChecklist = (resumeId, sectionKey, completed) =>
  apiClient.put("/api/resume-health/checklist", {
    resumeId,
    sectionKey,
    completed,
  });
