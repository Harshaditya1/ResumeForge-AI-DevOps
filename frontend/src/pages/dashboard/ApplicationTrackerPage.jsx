import { useCallback, useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  FaBriefcase,
  FaSearch,
  FaExternalLinkAlt,
  FaBookmark,
  FaPlus,
  FaCalendarAlt,
  FaMapMarkerAlt,
  FaEdit,
  FaTrash,
  FaCheckCircle,
  FaClock,
  FaGlobe,
  FaBuilding,
  FaFilter,
} from "react-icons/fa";
import {
  createApplication,
  deleteApplication,
  discoverJobs,
  getApplicationStatistics,
  getApplications,
  saveDiscoveredJob,
  updateApplication,
} from "../../services/applicationService";

const STATUSES = [
  "SAVED",
  "APPLIED",
  "ASSESSMENT",
  "INTERVIEW",
  "OFFER",
  "REJECTED",
];

const STATUS_COLORS = {
  SAVED: "text-blue-400 bg-blue-500/10 border-blue-500/30",
  APPLIED: "text-amber-400 bg-amber-500/10 border-amber-500/30",
  ASSESSMENT: "text-purple-400 bg-purple-500/10 border-purple-500/30",
  INTERVIEW: "text-cyan-400 bg-cyan-500/10 border-cyan-500/30",
  OFFER: "text-emerald-400 bg-emerald-500/10 border-emerald-500/30",
  REJECTED: "text-rose-400 bg-rose-500/10 border-rose-500/30",
};

const TARGET_ROLES = [
  { id: "ALL", label: "All Roles" },
  { id: "Java", label: "Java Developer" },
  { id: "Spring Boot", label: "Spring Boot Developer" },
  { id: "Software Engineer", label: "Software Engineer" },
  { id: "Android", label: "Android Developer" },
  { id: "Backend", label: "Backend Developer" },
  { id: "Full Stack", label: "Full Stack Developer" },
];

const TARGET_LOCATIONS = [
  { id: "ALL", label: "All Locations" },
  { id: "India", label: "India" },
  { id: "Remote", label: "Remote" },
  { id: "Bengaluru", label: "Bengaluru" },
  { id: "Noida", label: "Noida / Delhi NCR" },
];

const emptyForm = {
  companyName: "",
  jobTitle: "",
  location: "",
  experienceLevel: "Fresher / 0-1 Years",
  jobType: "Full-time",
  source: "Manual",
  applicationUrl: "",
  appliedDate: "",
  interviewDate: "",
  deadline: "",
  status: "SAVED",
  notes: "",
};

export default function ApplicationTrackerPage() {
  const [activeTab, setActiveTab] = useState("DISCOVER"); // "DISCOVER" or "TRACKER"
  const [applications, setApplications] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [discoveredJobs, setDiscoveredJobs] = useState([]);
  const [loadingDiscovery, setLoadingDiscovery] = useState(true);
  const [loadingApplications, setLoadingApplications] = useState(true);

  // Filters for Discovery
  const [discRole, setDiscRole] = useState("ALL");
  const [discLocation, setDiscLocation] = useState("ALL");
  const [discJobType, setDiscJobType] = useState("ALL");
  const [discSearch, setDiscSearch] = useState("");

  // Filters for Tracker
  const [trackSearch, setTrackSearch] = useState("");
  const [trackStatus, setTrackStatus] = useState("ALL");

  // Form State
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [savingJobId, setSavingJobId] = useState(null);

  // Load tracker applications and stats
  const loadTrackerData = useCallback(async () => {
    try {
      setLoadingApplications(true);
      const [apps, stats] = await Promise.all([
        getApplications(),
        getApplicationStatistics(),
      ]);
      setApplications(apps || []);
      setStatistics(stats);
    } catch (err) {
      console.error(err);
      toast.error(err?.response?.data?.message || "Failed to load applications.");
    } finally {
      setLoadingApplications(false);
    }
  }, []);

  // Load discovery jobs
  const loadDiscoveryData = useCallback(async () => {
    try {
      setLoadingDiscovery(true);
      const params = {};
      if (discRole !== "ALL") params.role = discRole;
      if (discLocation !== "ALL") params.location = discLocation;
      if (discJobType !== "ALL") params.jobType = discJobType;
      if (discSearch && discSearch.trim()) params.search = discSearch.trim();

      const jobs = await discoverJobs(params);
      setDiscoveredJobs(jobs || []);
    } catch (err) {
      console.error(err);
      toast.error(err?.response?.data?.message || "Failed to fetch live job opportunities.");
    } finally {
      setLoadingDiscovery(false);
    }
  }, [discRole, discLocation, discJobType, discSearch]);

  useEffect(() => {
    loadTrackerData();
  }, [loadTrackerData]);

  useEffect(() => {
    loadDiscoveryData();
  }, [loadDiscoveryData]);

  // Handle Save Discovered Job to Applications
  const handleSaveDiscovered = async (job) => {
    try {
      setSavingJobId(job.id);
      await saveDiscoveredJob(job);
      toast.success(`Saved "${job.jobTitle}" at ${job.companyName} to your Tracker!`);
      // Update local discovery item to show alreadySaved
      setDiscoveredJobs((prev) =>
        prev.map((item) => (item.id === job.id ? { ...item, alreadySaved: true } : item))
      );
      await loadTrackerData();
    } catch (err) {
      const msg = err?.response?.data?.message || "Unable to save job to tracker.";
      toast.error(msg);
    } finally {
      setSavingJobId(null);
    }
  };

  // Open Add Manual Application Modal
  const openCreateModal = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  // Open Edit Application Modal
  const openEditModal = (app) => {
    setEditingId(app.id);
    setForm({
      companyName: app.companyName || "",
      jobTitle: app.jobTitle || "",
      location: app.location || "",
      experienceLevel: app.experienceLevel || "Fresher / 0-1 Years",
      jobType: app.jobType || "Full-time",
      source: app.source || "Manual",
      applicationUrl: app.applicationUrl || "",
      appliedDate: app.appliedDate || "",
      interviewDate: app.interviewDate || "",
      deadline: app.deadline || "",
      status: app.status || "SAVED",
      notes: app.notes || "",
    });
    setShowModal(true);
  };

  // Handle Form Submit
  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!form.companyName.trim() || !form.jobTitle.trim()) {
      toast.error("Company name and job title are required.");
      return;
    }

    try {
      setSaving(true);
      if (editingId) {
        await updateApplication(editingId, form);
        toast.success("Application updated successfully.");
      } else {
        await createApplication(form);
        toast.success("Application added to tracker.");
      }
      setShowModal(false);
      await loadTrackerData();
      await loadDiscoveryData();
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to save application.";
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  // Quick Status Change directly from card
  const handleQuickStatusChange = async (app, newStatus) => {
    try {
      const updatedPayload = {
        ...app,
        status: newStatus,
        appliedDate: newStatus === "APPLIED" && !app.appliedDate ? new Date().toISOString().split("T")[0] : app.appliedDate,
      };
      await updateApplication(app.id, updatedPayload);
      toast.success(`Updated status to ${newStatus}`);
      await loadTrackerData();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Failed to update status.");
    }
  };

  // Delete Application
  const handleDeleteApplication = async (id) => {
    if (!window.confirm("Are you sure you want to delete this application?")) return;
    try {
      await deleteApplication(id);
      toast.success("Application removed from tracker.");
      await loadTrackerData();
      await loadDiscoveryData();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Failed to delete application.");
    }
  };

  // Filtered Tracker Applications
  const filteredApplications = useMemo(() => {
    const q = trackSearch.trim().toLowerCase();
    return applications.filter((app) => {
      const matchSearch =
        !q ||
        app.companyName.toLowerCase().includes(q) ||
        app.jobTitle.toLowerCase().includes(q) ||
        (app.location && app.location.toLowerCase().includes(q));
      const matchStatus = trackStatus === "ALL" || app.status === trackStatus;
      return matchSearch && matchStatus;
    });
  }, [applications, trackSearch, trackStatus]);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
            <FaBriefcase className="text-xl" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Job Application Tracker
            </h1>
            <p className="text-sm text-[#A7B8B5] mt-0.5">
              Discover real fresher & internship roles, apply directly, and manage your pipeline.
            </p>
          </div>
        </div>

        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#408A71] text-white font-semibold text-sm hover:bg-[#34715c] transition shadow-sm self-start sm:self-auto"
        >
          <FaPlus />
          <span>Add Application</span>
        </button>
      </div>

      {/* Main Tabs (Job Discovery vs My Applications) */}
      <div className="flex border-b border-white/10 gap-2">
        <button
          onClick={() => setActiveTab("DISCOVER")}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-semibold border-b-2 transition ${
            activeTab === "DISCOVER"
              ? "border-[#408A71] text-white bg-white/[0.03]"
              : "border-transparent text-[#A7B8B5] hover:text-white"
          }`}
        >
          <FaGlobe className={activeTab === "DISCOVER" ? "text-[#B0E4CC]" : ""} />
          <span>Live Job Discovery</span>
          <span className="ml-1.5 px-2 py-0.5 rounded-full text-[10px] bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
            Fresher & Intern
          </span>
        </button>

        <button
          onClick={() => setActiveTab("TRACKER")}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-semibold border-b-2 transition ${
            activeTab === "TRACKER"
              ? "border-[#408A71] text-white bg-white/[0.03]"
              : "border-transparent text-[#A7B8B5] hover:text-white"
          }`}
        >
          <FaBookmark className={activeTab === "TRACKER" ? "text-[#B0E4CC]" : ""} />
          <span>My Applications Tracker</span>
          <span className="ml-1.5 px-2 py-0.5 rounded-full text-[10px] bg-white/10 text-white font-bold">
            {applications.length}
          </span>
        </button>
      </div>

      {/* TAB 1: JOB DISCOVERY */}
      {activeTab === "DISCOVER" && (
        <div className="space-y-6">
          {/* Discovery Filter Controls */}
          <div className="rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur-sm space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              {/* Search */}
              <div className="relative md:col-span-2">
                <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#A7B8B5]/60" />
                <input
                  type="text"
                  value={discSearch}
                  onChange={(e) => setDiscSearch(e.target.value)}
                  placeholder="Search title, company, or skills (e.g., Spring Boot, React)..."
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-white/10 bg-black/20 text-white placeholder-[#A7B8B5]/50 outline-none focus:border-[#408A71] text-sm"
                />
              </div>

              {/* Target Role Dropdown */}
              <select
                value={discRole}
                onChange={(e) => setDiscRole(e.target.value)}
                className="px-3.5 py-2.5 rounded-xl border border-white/10 bg-[#18352c] text-white text-sm outline-none focus:border-[#408A71]"
              >
                {TARGET_ROLES.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.label}
                  </option>
                ))}
              </select>

              {/* Target Location Dropdown */}
              <select
                value={discLocation}
                onChange={(e) => setDiscLocation(e.target.value)}
                className="px-3.5 py-2.5 rounded-xl border border-white/10 bg-[#18352c] text-white text-sm outline-none focus:border-[#408A71]"
              >
                {TARGET_LOCATIONS.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Quick Job Type Toggles */}
            <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-white/5 text-xs text-[#A7B8B5]">
              <div className="flex items-center gap-2">
                <span className="font-medium text-white">Experience / Type:</span>
                <button
                  onClick={() => setDiscJobType("ALL")}
                  className={`px-3 py-1 rounded-lg border transition ${
                    discJobType === "ALL"
                      ? "bg-[#408A71] text-white border-[#408A71]"
                      : "bg-white/5 text-[#A7B8B5] border-white/10 hover:text-white"
                  }`}
                >
                  All Types
                </button>
                <button
                  onClick={() => setDiscJobType("Internship")}
                  className={`px-3 py-1 rounded-lg border transition ${
                    discJobType === "Internship"
                      ? "bg-[#408A71] text-white border-[#408A71]"
                      : "bg-white/5 text-[#A7B8B5] border-white/10 hover:text-white"
                  }`}
                >
                  Internships
                </button>
                <button
                  onClick={() => setDiscJobType("Full-time")}
                  className={`px-3 py-1 rounded-lg border transition ${
                    discJobType === "Full-time"
                      ? "bg-[#408A71] text-white border-[#408A71]"
                      : "bg-white/5 text-[#A7B8B5] border-white/10 hover:text-white"
                  }`}
                >
                  Full-Time (Fresher)
                </button>
              </div>

              <span>Showing {discoveredJobs.length} opportunities</span>
            </div>
          </div>

          {/* Job Discovery Cards */}
          {loadingDiscovery ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className="h-48 rounded-2xl border border-white/10 bg-white/5 animate-pulse"
                />
              ))}
            </div>
          ) : discoveredJobs.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-12 text-center">
              <FaBriefcase className="mx-auto text-4xl text-[#A7B8B5]/40 mb-3" />
              <h3 className="text-lg font-semibold text-white">No job openings found</h3>
              <p className="text-sm text-[#A7B8B5] mt-1 max-w-md mx-auto">
                No matching opportunities found for your current filter settings. Try expanding your search.
              </p>
              <button
                onClick={() => {
                  setDiscRole("ALL");
                  setDiscLocation("ALL");
                  setDiscJobType("ALL");
                  setDiscSearch("");
                }}
                className="mt-4 px-4 py-2 rounded-xl bg-[#408A71] text-white text-xs font-semibold hover:bg-[#34715c] transition"
              >
                Reset Discovery Filters
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {discoveredJobs.map((job) => (
                <div
                  key={job.id}
                  className="rounded-2xl border border-white/10 bg-white/5 p-5 flex flex-col justify-between hover:border-white/20 transition backdrop-blur-sm"
                >
                  <div>
                    {/* Badges */}
                    <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        {job.isLive ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping" />
                            Live Feed
                          </span>
                        ) : (
                          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-blue-500/20 text-blue-300 border border-blue-500/30">
                            Verified Opening
                          </span>
                        )}

                        <span className="px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-white/5 text-[#A7B8B5] border border-white/10">
                          {job.source}
                        </span>
                      </div>

                      {job.postedDate && (
                        <span className="text-[11px] text-[#A7B8B5]/80 flex items-center gap-1">
                          <FaCalendarAlt className="text-[9px]" />
                          {job.postedDate}
                        </span>
                      )}
                    </div>

                    {/* Job Title & Company */}
                    <h3 className="text-base font-bold text-white line-clamp-1">
                      {job.jobTitle}
                    </h3>
                    <div className="flex items-center gap-2 mt-1 text-sm text-[#B0E4CC] font-medium">
                      <FaBuilding className="text-xs text-[#408A71]" />
                      <span>{job.companyName}</span>
                    </div>

                    {/* Location, Job Type, Experience */}
                    <div className="flex flex-wrap items-center gap-3 mt-3 text-xs text-[#A7B8B5]">
                      <span className="flex items-center gap-1">
                        <FaMapMarkerAlt className="text-[#408A71]" />
                        {job.location}
                      </span>
                      <span>•</span>
                      <span>{job.jobType}</span>
                      <span>•</span>
                      <span className="text-white/80">{job.experienceLevel}</span>
                    </div>

                    {/* Description snippet */}
                    {job.description && (
                      <p className="mt-3 text-xs text-[#A7B8B5] line-clamp-2 leading-relaxed">
                        {job.description}
                      </p>
                    )}
                  </div>

                  {/* Actions Footer */}
                  <div className="mt-5 pt-4 border-t border-white/5 flex items-center justify-between gap-3">
                    <button
                      onClick={() => handleSaveDiscovered(job)}
                      disabled={job.alreadySaved || savingJobId === job.id}
                      className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-semibold transition ${
                        job.alreadySaved
                          ? "bg-white/5 text-emerald-400 border border-emerald-500/30 cursor-default"
                          : "bg-white/10 text-white border border-white/10 hover:bg-white/20"
                      }`}
                    >
                      {job.alreadySaved ? (
                        <>
                          <FaCheckCircle className="text-emerald-400" />
                          <span>In Tracker</span>
                        </>
                      ) : (
                        <>
                          <FaBookmark />
                          <span>Save to Tracker</span>
                        </>
                      )}
                    </button>

                    {job.applicationUrl && (
                      <a
                        href={job.applicationUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#408A71] text-white text-xs font-semibold hover:bg-[#34715c] transition shadow-sm"
                      >
                        <span>Apply Now</span>
                        <FaExternalLinkAlt className="text-[10px]" />
                      </a>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: MY APPLICATIONS TRACKER */}
      {activeTab === "TRACKER" && (
        <div className="space-y-6">
          {/* Status Distribution Statistics */}
          {statistics && (
            <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
              <div className="p-4 rounded-2xl border border-white/10 bg-white/5">
                <p className="text-xs text-[#A7B8B5]">Total</p>
                <p className="text-2xl font-bold text-white mt-1">
                  {statistics.total || 0}
                </p>
              </div>

              {STATUSES.map((st) => {
                const count = statistics.byStatus ? statistics.byStatus[st] || 0 : 0;
                return (
                  <button
                    key={st}
                    onClick={() => setTrackStatus(trackStatus === st ? "ALL" : st)}
                    className={`text-left p-4 rounded-2xl border transition ${
                      trackStatus === st
                        ? "bg-[#408A71]/20 border-[#408A71]"
                        : "bg-white/5 border-white/10 hover:border-white/20"
                    }`}
                  >
                    <p className="text-[11px] font-semibold text-[#A7B8B5]">{st}</p>
                    <p className="text-2xl font-bold text-white mt-1">{count}</p>
                  </button>
                );
              })}
            </div>
          )}

          {/* Tracker Filters */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#A7B8B5]/60" />
              <input
                type="text"
                value={trackSearch}
                onChange={(e) => setTrackSearch(e.target.value)}
                placeholder="Search saved applications by company, title, or location..."
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-white/10 bg-white/5 text-white placeholder-[#A7B8B5]/50 outline-none focus:border-[#408A71] text-sm"
              />
            </div>

            <select
              value={trackStatus}
              onChange={(e) => setTrackStatus(e.target.value)}
              className="px-3.5 py-2.5 rounded-xl border border-white/10 bg-[#18352c] text-white text-sm outline-none focus:border-[#408A71]"
            >
              <option value="ALL">All Statuses ({applications.length})</option>
              {STATUSES.map((st) => (
                <option key={st} value={st}>
                  {st}
                </option>
              ))}
            </select>
          </div>

          {/* Applications List */}
          {loadingApplications ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="h-28 rounded-2xl border border-white/10 bg-white/5 animate-pulse"
                />
              ))}
            </div>
          ) : filteredApplications.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-12 text-center">
              <FaBriefcase className="mx-auto text-4xl text-[#A7B8B5]/40 mb-3" />
              <h3 className="text-lg font-semibold text-white">No applications tracked</h3>
              <p className="text-sm text-[#A7B8B5] mt-1 max-w-md mx-auto">
                {applications.length === 0
                  ? "Start tracking your journey by saving jobs from Live Discovery or adding manually."
                  : "No applications match your search or status filter."}
              </p>
              <div className="mt-4 flex items-center justify-center gap-3">
                <button
                  onClick={() => setActiveTab("DISCOVER")}
                  className="px-4 py-2 rounded-xl bg-[#408A71] text-white text-xs font-semibold hover:bg-[#34715c] transition"
                >
                  Browse Live Discovery
                </button>
                <button
                  onClick={openCreateModal}
                  className="px-4 py-2 rounded-xl border border-white/20 text-white text-xs font-semibold hover:bg-white/10 transition"
                >
                  Add Manually
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredApplications.map((app) => {
                const statusStyle =
                  STATUS_COLORS[app.status] ||
                  "text-emerald-400 bg-emerald-500/10 border-emerald-500/30";

                return (
                  <article
                    key={app.id}
                    className="rounded-2xl border border-white/10 bg-white/5 p-5 hover:border-white/20 transition backdrop-blur-sm"
                  >
                    <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                      {/* Left: Job Info */}
                      <div className="space-y-1.5 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="text-lg font-bold text-white">
                            {app.jobTitle}
                          </h3>
                          <span className="text-[#A7B8B5]">at</span>
                          <span className="text-base font-semibold text-[#B0E4CC]">
                            {app.companyName}
                          </span>

                          {/* Quick Status Dropdown */}
                          <select
                            value={app.status}
                            onChange={(e) => handleQuickStatusChange(app, e.target.value)}
                            className={`ml-2 px-2.5 py-0.5 rounded-md text-xs font-bold border outline-none bg-black/40 cursor-pointer ${statusStyle}`}
                          >
                            {STATUSES.map((st) => (
                              <option key={st} value={st} className="bg-[#18352c] text-white">
                                {st}
                              </option>
                            ))}
                          </select>
                        </div>

                        {/* Location, Experience, Job Type */}
                        <div className="flex flex-wrap items-center gap-3 text-xs text-[#A7B8B5]">
                          {app.location && (
                            <span className="flex items-center gap-1">
                              <FaMapMarkerAlt className="text-[#408A71]" />
                              {app.location}
                            </span>
                          )}
                          {app.jobType && (
                            <>
                              <span>•</span>
                              <span>{app.jobType}</span>
                            </>
                          )}
                          {app.experienceLevel && (
                            <>
                              <span>•</span>
                              <span>{app.experienceLevel}</span>
                            </>
                          )}
                          {app.source && (
                            <>
                              <span>•</span>
                              <span className="text-white/60">Source: {app.source}</span>
                            </>
                          )}
                        </div>

                        {/* Important Dates: Applied, Interview, Deadline */}
                        <div className="flex flex-wrap items-center gap-4 pt-1 text-xs">
                          {app.appliedDate && (
                            <span className="text-[#A7B8B5] flex items-center gap-1">
                              <FaCalendarAlt className="text-[#408A71]" />
                              Applied: <span className="text-white">{app.appliedDate}</span>
                            </span>
                          )}
                          {app.interviewDate && (
                            <span className="text-cyan-300 font-semibold flex items-center gap-1 bg-cyan-500/10 px-2 py-0.5 rounded border border-cyan-500/30">
                              <FaClock />
                              Interview: {app.interviewDate}
                            </span>
                          )}
                          {app.deadline && (
                            <span className="text-amber-300 font-semibold flex items-center gap-1 bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/30">
                              <FaClock />
                              Deadline: {app.deadline}
                            </span>
                          )}
                        </div>

                        {/* Notes */}
                        {app.notes && (
                          <div className="mt-2 text-xs text-[#D8E3DF] bg-black/20 p-2.5 rounded-xl border border-white/5">
                            <span className="font-semibold text-[#B0E4CC]">Notes: </span>
                            {app.notes}
                          </div>
                        )}
                      </div>

                      {/* Right: Actions */}
                      <div className="flex items-center gap-2 self-end lg:self-center">
                        {app.applicationUrl && (
                          <a
                            href={app.applicationUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="p-2.5 rounded-xl border border-white/10 bg-white/5 text-[#B0E4CC] hover:bg-white/10 transition"
                            title="Open Application Link"
                          >
                            <FaExternalLinkAlt className="text-xs" />
                          </a>
                        )}

                        <button
                          onClick={() => openEditModal(app)}
                          className="p-2.5 rounded-xl border border-white/10 bg-white/5 text-white hover:bg-white/10 transition"
                          title="Edit Application"
                        >
                          <FaEdit className="text-xs" />
                        </button>

                        <button
                          onClick={() => handleDeleteApplication(app.id)}
                          className="p-2.5 rounded-xl border border-white/10 bg-white/5 text-rose-400 hover:bg-rose-500/20 hover:border-rose-500/30 transition"
                          title="Delete Application"
                        >
                          <FaTrash className="text-xs" />
                        </button>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* MODAL: ADD / EDIT APPLICATION */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="w-full max-w-xl rounded-2xl border border-white/10 bg-[#0B1914] p-6 shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <h2 className="text-lg font-bold text-white">
                {editingId ? "Edit Job Application" : "Add Job Application"}
              </h2>
              <button
                onClick={() => setShowModal(false)}
                className="text-[#A7B8B5] hover:text-white"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleFormSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Company Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={form.companyName}
                    onChange={(e) => setForm({ ...form, companyName: e.target.value })}
                    placeholder="e.g. Infosys, TCS, PhonePe"
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Job Title *
                  </label>
                  <input
                    type="text"
                    required
                    value={form.jobTitle}
                    onChange={(e) => setForm({ ...form, jobTitle: e.target.value })}
                    placeholder="e.g. Java Developer, Software Engineer"
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Location
                  </label>
                  <input
                    type="text"
                    value={form.location}
                    onChange={(e) => setForm({ ...form, location: e.target.value })}
                    placeholder="e.g. Bengaluru, Remote, Noida"
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Job Type
                  </label>
                  <select
                    value={form.jobType}
                    onChange={(e) => setForm({ ...form, jobType: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-[#18352c] text-white outline-none focus:border-[#408A71] text-sm"
                  >
                    <option value="Full-time">Full-time</option>
                    <option value="Internship">Internship</option>
                    <option value="Contract">Contract</option>
                    <option value="Part-time">Part-time</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Status
                  </label>
                  <select
                    value={form.status}
                    onChange={(e) => setForm({ ...form, status: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-[#18352c] text-white outline-none focus:border-[#408A71] text-sm"
                  >
                    {STATUSES.map((st) => (
                      <option key={st} value={st}>
                        {st}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Applied Date
                  </label>
                  <input
                    type="date"
                    value={form.appliedDate}
                    onChange={(e) => setForm({ ...form, appliedDate: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Interview Date
                  </label>
                  <input
                    type="date"
                    value={form.interviewDate}
                    onChange={(e) => setForm({ ...form, interviewDate: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                    Deadline
                  </label>
                  <input
                    type="date"
                    value={form.deadline}
                    onChange={(e) => setForm({ ...form, deadline: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                  Application / Job Posting URL
                </label>
                <input
                  type="url"
                  value={form.applicationUrl}
                  onChange={(e) => setForm({ ...form, applicationUrl: e.target.value })}
                  placeholder="https://company.com/careers/job-id"
                  className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#A7B8B5] mb-1">
                  Notes & Interview Prep Details
                </label>
                <textarea
                  rows="3"
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                  placeholder="Recruiter contact, round details, tech stack questions..."
                  className="w-full px-3 py-2 rounded-xl border border-white/10 bg-white/5 text-white outline-none focus:border-[#408A71] text-sm resize-none"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-white/10">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-xl border border-white/10 text-[#A7B8B5] hover:text-white text-xs font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2 rounded-xl bg-[#408A71] text-white text-xs font-semibold hover:bg-[#34715c] transition"
                >
                  {saving ? "Saving..." : editingId ? "Update Application" : "Save Application"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
