import { getCached } from "./apiClient";
import apiClient from "./apiClient";

export const getInterviewQuestions = (params = {}) =>
  getCached("/api/interview-prep/questions", { params });

export const getInterviewProgress = () =>
  getCached("/api/interview-prep/progress");

export const updateInterviewProgress = (questionId, completed, favorite) =>
  apiClient.post("/api/interview-prep/progress", {
    questionId,
    completed,
    favorite,
  });
