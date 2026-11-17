import {
  FaHome,
  FaFileUpload,
  FaClipboardList,
  FaChartLine,
  FaBriefcase,
  FaHeartbeat,
  FaHistory,
  FaBookOpen,
  FaSignOutAlt,
} from "react-icons/fa";

export const sidebarMenu = [
  {
    title: "Dashboard",
    icon: FaHome,
    path: "/dashboard",
  },
  {
    title: "Upload Resume",
    icon: FaFileUpload,
    path: "/dashboard/resume-upload",
  },
  {
    title: "Job Description",
    icon: FaClipboardList,
    path: "/dashboard/job-description",
  },
  {
    title: "Resume Analysis",
    icon: FaChartLine,
    path: "/dashboard/analysis",
  },
  {
    title: "Job Application Tracker",
    icon: FaBriefcase,
    path: "/dashboard/applications",
  },
  {
    title: "Resume Health Center",
    icon: FaHeartbeat,
    path: "/dashboard/resume-health",
  },
  {
    title: "Resume History",
    icon: FaHistory,
    path: "/dashboard/history",
  },
  {
    title: "Interview Preparation Hub",
    icon: FaBookOpen,
    path: "/dashboard/interview-prep",
  },
  {
    title: "Logout",
    icon: FaSignOutAlt,
    path: "/logout",
  },
];