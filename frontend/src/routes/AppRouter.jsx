import {
  BrowserRouter,
  Navigate,
  Routes,
  Route,
} from "react-router-dom";

import LandingPage from "../pages/landing/LandingPage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import ResumeUploadPage from "../pages/dashboard/ResumeUploadPage";

import DashboardLayout from "../layout/DashboardLayout";
import ProtectedRoute from "./ProtectedRoute";
import JobDescriptionPage from "../pages/dashboard/JobDescriptionPage";
import ResumeAnalysisPage from "../pages/dashboard/ResumeAnalysisPage";
import HistoryPage from "../pages/dashboard/HistoryPage";
import ApplicationTrackerPage from "../pages/dashboard/ApplicationTrackerPage";
import ResumeHealthPage from "../pages/dashboard/ResumeHealthPage";
import InterviewPreparationPage from "../pages/dashboard/InterviewPreparationPage";
import { useAuth } from "../context/AuthContext";

function LogoutRoute() {
  const { logout } = useAuth();
  logout();
  return <Navigate to="/login" replace />;
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/dashboard/resume-upload"
              element={<ResumeUploadPage />}
            />
            <Route
              path="/dashboard/job-description"
              element={<JobDescriptionPage />}
            />
            <Route
              path="/dashboard/analysis"
              element={<ResumeAnalysisPage />}
            />
            <Route
              path="/dashboard/applications"
              element={<ApplicationTrackerPage />}
            />
            <Route
              path="/dashboard/resume-health"
              element={<ResumeHealthPage />}
            />
            <Route path="/dashboard/history" element={<HistoryPage />} />
            <Route
              path="/dashboard/interview-prep"
              element={<InterviewPreparationPage />}
            />
          </Route>
        </Route>
        <Route path="/logout" element={<LogoutRoute />} />
      </Routes>
    </BrowserRouter>
  );
}