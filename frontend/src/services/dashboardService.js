import { getCached } from "./apiClient";

const dashboardService = {
  async getDashboardSummary() {
    return getCached("/api/dashboard/summary");
  },
};

export default dashboardService;