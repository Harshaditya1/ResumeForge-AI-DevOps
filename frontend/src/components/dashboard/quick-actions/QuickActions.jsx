import {
  FaFileUpload,
  FaClipboardList,
  FaChartLine,
  FaBriefcase,
  FaHeartbeat,
  FaBookOpen,
} from "react-icons/fa";

import QuickActionCard from "./QuickActionCard";

const actions = [
  {
    title: "Upload Resume",
    description: "Upload your resume in PDF or DOCX format.",
    icon: FaFileUpload,
    color: "#408A71",
    path: "/dashboard/resume-upload",
  },
  {
    title: "Resume Health Center",
    description: "Inspect parsed resume data and completeness diagnostics.",
    icon: FaHeartbeat,
    color: "#10B981",
    path: "/dashboard/resume-health",
  },
  {
    title: "Resume Analysis",
    description: "Analyze ATS score and target keyword matching.",
    icon: FaChartLine,
    color: "#F59E0B",
    path: "/dashboard/analysis",
  },
  {
    title: "Job Application Tracker",
    description: "Discover fresher tech jobs and track application stages.",
    icon: FaBriefcase,
    color: "#8B5CF6",
    path: "/dashboard/applications",
  },
  {
    title: "Interview Preparation",
    description: "Practice real questions from LeetCode, GFG & Java Docs.",
    icon: FaBookOpen,
    color: "#2F80ED",
    path: "/dashboard/interview-prep",
  },
  {
    title: "Job Description",
    description: "Paste or manage targeted job descriptions.",
    icon: FaClipboardList,
    color: "#EC4899",
    path: "/dashboard/job-description",
  },
];

export default function QuickActions() {
  return (
    <section>
      <h2 className="text-2xl font-semibold text-white mb-6">
        Quick Actions
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        {actions.map((action) => (
          <QuickActionCard
            key={action.title}
            {...action}
          />
        ))}
      </div>
    </section>
  );
}
