import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import {
  FaHeartbeat,
  FaFileUpload,
  FaUser,
  FaEnvelope,
  FaPhone,
  FaGraduationCap,
  FaBriefcase,
  FaCode,
  FaProjectDiagram,
  FaCheckCircle,
  FaExclamationTriangle,
  FaChartLine,
  FaArrowRight,
  FaInfoCircle,
  FaListUl,
} from "react-icons/fa";
import {
  getLatestResumeHealth,
  updateResumeHealthChecklist,
} from "../../services/resumeHealthService";

export default function ResumeHealthPage() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [savingKey, setSavingKey] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    getLatestResumeHealth()
      .then((data) => setHealth(data))
      .catch((error) => {
        // If 404 or no resume uploaded, health remains null
        console.warn("No resume health found:", error?.message);
      })
      .finally(() => setLoading(false));
  }, []);

  const toggleChecklist = async (section) => {
    if (!health) return;
    try {
      setSavingKey(section.key);
      const response = await updateResumeHealthChecklist(
        health.resumeId,
        section.key,
        !section.checklistCompleted
      );
      setHealth(response.data);
      toast.success(
        !section.checklistCompleted
          ? `Checked off "${section.label}" improvement`
          : `Marked "${section.label}" as pending`
      );
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Unable to update improvement checklist."
      );
    } finally {
      setSavingKey(null);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-20 rounded-2xl border border-white/10 bg-white/5 animate-pulse" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="h-48 rounded-2xl border border-white/10 bg-white/5 animate-pulse" />
          <div className="h-48 rounded-2xl border border-white/10 bg-white/5 animate-pulse" />
          <div className="h-48 rounded-2xl border border-white/10 bg-white/5 animate-pulse" />
        </div>
      </div>
    );
  }

  // EMPTY STATE: No resume uploaded yet
  if (!health) {
    return (
      <div className="space-y-8">
        {/* Header */}
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
            <FaHeartbeat className="text-xl" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Resume Health Center
            </h1>
            <p className="text-sm text-[#A7B8B5] mt-0.5">
              Evidence-based completeness score, parsed candidate information, and ATS diagnostics.
            </p>
          </div>
        </div>

        {/* Empty State Banner */}
        <div className="rounded-2xl border border-dashed border-white/15 bg-white/[0.03] p-10 sm:p-14 text-center max-w-2xl mx-auto backdrop-blur-sm">
          <div className="w-16 h-16 rounded-2xl bg-[#408A71]/20 border border-[#408A71]/40 flex items-center justify-center mx-auto text-[#B0E4CC] text-2xl mb-5 shadow-lg">
            <FaFileUpload />
          </div>

          <h2 className="text-xl sm:text-2xl font-bold text-white mb-2">
            No Resume Uploaded Yet
          </h2>

          <p className="text-sm text-[#A7B8B5] leading-relaxed max-w-lg mx-auto mb-6">
            Uploading your resume in PDF or DOCX format enables instant section-by-section health analysis,
            candidate data verification, ATS compatibility diagnostics, and an actionable improvement checklist.
          </p>

          <Link
            to="/dashboard/resume-upload"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-[#408A71] text-white font-semibold text-sm hover:bg-[#34715c] transition shadow-md"
          >
            <FaFileUpload />
            <span>Upload Resume Now</span>
          </Link>
        </div>
      </div>
    );
  }

  // POPULATED RESUME HEALTH STATE
  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
            <FaHeartbeat className="text-xl" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Resume Health Center
            </h1>
            <p className="text-sm text-[#A7B8B5] mt-0.5">
              Live diagnostics extracted from <span className="text-white font-medium">{health.fileName}</span>
            </p>
          </div>
        </div>

        <Link
          to="/dashboard/resume-upload"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-white/15 bg-white/5 text-[#D8E3DF] hover:text-white hover:border-[#408A71] text-xs font-semibold transition self-start sm:self-auto"
        >
          <FaFileUpload />
          <span>Upload New Version</span>
        </Link>
      </div>

      {/* Top Scores Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {/* Card 1: Resume Completeness Score */}
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm relative overflow-hidden flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#A7B8B5]">
              Completeness Score
            </span>
            <span className="p-2 rounded-lg bg-[#408A71]/20 text-[#B0E4CC]">
              <FaCheckCircle className="text-sm" />
            </span>
          </div>

          <div className="my-4 flex items-baseline gap-3">
            <span className="text-5xl font-black text-[#B0E4CC] tracking-tight">
              {health.score}%
            </span>
            <span className="text-xs text-[#A7B8B5]">
              {health.completedSections} of {health.totalSections} sections detected
            </span>
          </div>

          <div className="w-full h-2 rounded-full bg-black/40 overflow-hidden border border-white/5">
            <div
              className="h-full bg-gradient-to-r from-[#2F80ED] to-[#408A71] rounded-full transition-all duration-500"
              style={{ width: `${health.score}%` }}
            />
          </div>
        </div>

        {/* Card 2: ATS Score from Analysis */}
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm relative overflow-hidden flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#A7B8B5]">
              ATS Match Score
            </span>
            <span className="p-2 rounded-lg bg-[#2F80ED]/20 text-[#2F80ED]">
              <FaChartLine className="text-sm" />
            </span>
          </div>

          {health.hasAtsAnalysis ? (
            <div>
              <div className="my-4 flex items-baseline gap-3">
                <span className="text-5xl font-black text-white tracking-tight">
                  {Math.round(health.atsScore || 0)}%
                </span>
                {health.atsScoreCategory && (
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    {health.atsScoreCategory}
                  </span>
                )}
              </div>
              <p className="text-xs text-[#A7B8B5] line-clamp-1">
                {health.atsOverallAssessment || "Calculated from target job description match."}
              </p>
            </div>
          ) : (
            <div className="my-2">
              <p className="text-sm font-semibold text-white">Not Yet Analyzed</p>
              <p className="text-xs text-[#A7B8B5] mt-1 mb-3">
                Run ATS analysis against a target job description to calculate match score.
              </p>
              <button
                onClick={() => navigate("/dashboard/analysis")}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#2F80ED] text-white text-xs font-semibold hover:bg-blue-600 transition"
              >
                <span>Run ATS Analysis</span>
                <FaArrowRight className="text-[10px]" />
              </button>
            </div>
          )}

          <div className="pt-2 text-xs text-[#A7B8B5]">
            {health.hasAtsAnalysis ? (
              <Link
                to="/dashboard/analysis"
                className="text-[#B0E4CC] hover:underline inline-flex items-center gap-1"
              >
                <span>View Full ATS Report</span>
                <FaArrowRight className="text-[9px]" />
              </Link>
            ) : (
              <span>Target JD keyword matching</span>
            )}
          </div>
        </div>

        {/* Card 3: Interactive Checklist Progress */}
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm relative overflow-hidden flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#A7B8B5]">
              Improvement Checklist
            </span>
            <span className="p-2 rounded-lg bg-amber-500/20 text-amber-400">
              <FaListUl className="text-sm" />
            </span>
          </div>

          <div className="my-4 flex items-baseline gap-3">
            <span className="text-5xl font-black text-amber-300 tracking-tight">
              {health.checklistCompleted}/{health.checklistTotal}
            </span>
            <span className="text-xs text-[#A7B8B5]">
              tasks completed
            </span>
          </div>

          <div className="w-full h-2 rounded-full bg-black/40 overflow-hidden border border-white/5">
            <div
              className="h-full bg-amber-400 rounded-full transition-all duration-500"
              style={{
                width: `${
                  health.checklistTotal > 0
                    ? (health.checklistCompleted / health.checklistTotal) * 100
                    : 0
                }%`,
              }}
            />
          </div>
        </div>
      </div>

      {/* EXTRACTED CANDIDATE INFORMATION CARD */}
      <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm space-y-5">
        <div className="flex items-center justify-between border-b border-white/10 pb-4">
          <div className="flex items-center gap-2">
            <FaUser className="text-[#408A71]" />
            <h2 className="text-base font-bold text-white">
              Extracted Resume Information
            </h2>
          </div>
          <span className="text-xs text-[#A7B8B5]">
            Parsed from uploaded file text
          </span>
        </div>

        {/* Contact info badges */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {/* Candidate Name */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1">
            <div className="flex items-center gap-2 text-xs text-[#A7B8B5]">
              <FaUser className="text-[#408A71]" />
              <span>Candidate Name</span>
            </div>
            <p className={`text-sm font-semibold ${health.candidateName === "Not Found" ? "text-amber-400/80" : "text-white"}`}>
              {health.candidateName}
            </p>
          </div>

          {/* Email */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1">
            <div className="flex items-center gap-2 text-xs text-[#A7B8B5]">
              <FaEnvelope className="text-[#408A71]" />
              <span>Email Address</span>
            </div>
            <p className={`text-sm font-semibold truncate ${health.email === "Not Found" ? "text-amber-400/80" : "text-white"}`}>
              {health.email}
            </p>
          </div>

          {/* Phone */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1">
            <div className="flex items-center gap-2 text-xs text-[#A7B8B5]">
              <FaPhone className="text-[#408A71]" />
              <span>Phone Number</span>
            </div>
            <p className={`text-sm font-semibold ${health.phone === "Not Found" ? "text-amber-400/80" : "text-white"}`}>
              {health.phone}
            </p>
          </div>
        </div>

        {/* Technical Skills Extracted */}
        <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-2">
          <div className="flex items-center gap-2 text-xs text-[#A7B8B5]">
            <FaCode className="text-[#408A71]" />
            <span>Extracted Technical Skills ({health.technicalSkills?.length || 0})</span>
          </div>

          {health.technicalSkills && health.technicalSkills.length > 0 ? (
            <div className="flex flex-wrap gap-2 pt-1">
              {health.technicalSkills.map((skill) => (
                <span
                  key={skill}
                  className="px-2.5 py-1 rounded-lg text-xs font-medium bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30"
                >
                  {skill}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-xs text-amber-400/80">
              Not Found — No recognized technical skill keywords detected in the parsed text.
            </p>
          )}
        </div>

        {/* Section Previews: Education, Experience, Projects */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-1">
          {/* Education Preview */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1.5">
            <div className="flex items-center gap-2 text-xs font-semibold text-white">
              <FaGraduationCap className="text-[#408A71]" />
              <span>Education Section</span>
            </div>
            <p className={`text-xs leading-relaxed ${health.educationPreview === "Not Found" ? "text-amber-400/80" : "text-[#A7B8B5]"}`}>
              {health.educationPreview}
            </p>
          </div>

          {/* Experience Preview */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1.5">
            <div className="flex items-center gap-2 text-xs font-semibold text-white">
              <FaBriefcase className="text-[#408A71]" />
              <span>Experience Section</span>
            </div>
            <p className={`text-xs leading-relaxed ${health.experiencePreview === "Not Found" ? "text-amber-400/80" : "text-[#A7B8B5]"}`}>
              {health.experiencePreview}
            </p>
          </div>

          {/* Projects Preview */}
          <div className="p-4 rounded-xl border border-white/10 bg-black/20 space-y-1.5">
            <div className="flex items-center gap-2 text-xs font-semibold text-white">
              <FaProjectDiagram className="text-[#408A71]" />
              <span>Projects Section</span>
            </div>
            <p className={`text-xs leading-relaxed ${health.projectsPreview === "Not Found" ? "text-amber-400/80" : "text-[#A7B8B5]"}`}>
              {health.projectsPreview}
            </p>
          </div>
        </div>
      </div>

      {/* SECTION DIAGNOSTICS & INTERACTIVE CHECKLIST */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column: Section Diagnostics & Weak Areas */}
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm space-y-4">
          <div className="flex items-center gap-2 border-b border-white/10 pb-3">
            <FaExclamationTriangle className="text-amber-400" />
            <h2 className="text-base font-bold text-white">
              Section Breakdown & Diagnostics
            </h2>
          </div>

          {/* Missing Sections Alert */}
          {health.missingSections && health.missingSections.length > 0 ? (
            <div className="p-4 rounded-xl border border-amber-500/30 bg-amber-500/10 space-y-1.5">
              <div className="flex items-center gap-2 text-xs font-bold text-amber-300">
                <FaExclamationTriangle />
                <span>Missing or Incomplete Sections Detected</span>
              </div>
              <p className="text-xs text-[#D8E3DF]">
                The following essential sections could not be found:{" "}
                <span className="font-semibold text-amber-200">
                  {health.missingSections.join(", ")}
                </span>
                . Adding these will significantly improve ATS pass rates.
              </p>
            </div>
          ) : (
            <div className="p-4 rounded-xl border border-emerald-500/30 bg-emerald-500/10 flex items-center gap-3">
              <FaCheckCircle className="text-emerald-400 text-lg flex-shrink-0" />
              <p className="text-xs text-emerald-200">
                All fundamental resume sections (Contact, Education, Experience, Projects, Skills) were successfully detected!
              </p>
            </div>
          )}

          {/* Section List */}
          <div className="space-y-3 pt-2">
            {health.sections?.map((sec) => (
              <div
                key={sec.key}
                className="p-3.5 rounded-xl border border-white/5 bg-black/20 flex flex-col justify-between gap-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    {sec.complete ? (
                      <FaCheckCircle className="text-emerald-400 text-xs" />
                    ) : (
                      <FaExclamationTriangle className="text-amber-400 text-xs" />
                    )}
                    <span className="text-sm font-semibold text-white">
                      {sec.label}
                    </span>
                  </div>

                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold border ${
                      sec.complete
                        ? "bg-emerald-500/20 text-emerald-300 border-emerald-500/30"
                        : "bg-amber-500/20 text-amber-300 border-amber-500/30"
                    }`}
                  >
                    {sec.complete ? "Detected" : "Needs Attention"} ({sec.weight}%)
                  </span>
                </div>

                <p className="text-xs text-[#A7B8B5]">
                  {sec.recommendation}
                </p>
              </div>
            ))}
          </div>

          {/* Actionable Improvement Recommendations */}
          {health.improvementRecommendations && health.improvementRecommendations.length > 0 && (
            <div className="pt-2 border-t border-white/5">
              <h3 className="text-xs font-bold text-white uppercase tracking-wider mb-2">
                Actionable Next Steps:
              </h3>
              <ul className="space-y-1.5">
                {health.improvementRecommendations.map((rec, i) => (
                  <li key={i} className="text-xs text-[#A7B8B5] flex items-start gap-2">
                    <span className="text-[#408A71] font-bold mt-0.5">•</span>
                    <span>{rec}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {/* Right Column: Interactive Improvement Checklist */}
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div className="flex items-center gap-2">
                <FaListUl className="text-[#408A71]" />
                <h2 className="text-base font-bold text-white">
                  Interactive Improvement Checklist
                </h2>
              </div>
              <span className="text-xs text-[#B0E4CC] font-semibold">
                {health.checklistCompleted} / {health.checklistTotal} done
              </span>
            </div>

            <p className="text-xs text-[#A7B8B5] mt-2 mb-4">
              Track changes you make to your resume before applying. Check off completed items to save your progress.
            </p>

            <div className="space-y-3">
              {health.sections?.map((sec) => (
                <label
                  key={sec.key}
                  className={`flex items-start gap-3 p-3.5 rounded-xl border transition cursor-pointer ${
                    sec.checklistCompleted
                      ? "border-emerald-500/30 bg-emerald-950/20"
                      : "border-white/10 bg-black/20 hover:border-white/20"
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={sec.checklistCompleted}
                    disabled={savingKey === sec.key}
                    onChange={() => toggleChecklist(sec)}
                    className="mt-0.5 w-4 h-4 rounded text-[#408A71] focus:ring-0 bg-transparent border-white/30 cursor-pointer"
                  />
                  <div className="text-xs">
                    <p className={`font-semibold ${sec.checklistCompleted ? "text-emerald-300 line-through" : "text-white"}`}>
                      Optimize {sec.label}
                    </p>
                    <p className="text-[#A7B8B5] mt-0.5">
                      {sec.recommendation}
                    </p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Footer Callout */}
          <div className="mt-6 pt-4 border-t border-white/10 flex items-center justify-between text-xs text-[#A7B8B5]">
            <span className="flex items-center gap-1.5">
              <FaInfoCircle className="text-[#408A71]" />
              Changes saved to your account in real-time.
            </span>
            <Link
              to="/dashboard/analysis"
              className="font-semibold text-[#B0E4CC] hover:underline flex items-center gap-1"
            >
              <span>Test with Job Description</span>
              <FaArrowRight className="text-[9px]" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
