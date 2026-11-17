import { useCallback, useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  createApplication,
  deleteApplication,
  getApplicationStatistics,
  getApplications,
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

const emptyForm = {
  companyName: "",
  jobTitle: "",
  jobDescription: "",
  applicationUrl: "",
  appliedDate: "",
  interviewDate: "",
  status: "SAVED",
  notes: "",
};

export default function ApplicationTrackerPage() {
  const [applications, setApplications] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [applicationData, statisticsData] = await Promise.all([
        getApplications(),
        getApplicationStatistics(),
      ]);
      setApplications(applicationData);
      setStatistics(statisticsData);
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Unable to load applications."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadData();
    }, 0);
    return () => clearTimeout(timer);
  }, [loadData]);

  const filteredApplications = useMemo(() => {
    const query = search.trim().toLowerCase();
    return applications.filter((application) => {
      const matchesSearch =
        !query ||
        application.companyName.toLowerCase().includes(query) ||
        application.jobTitle.toLowerCase().includes(query);
      const matchesStatus =
        statusFilter === "ALL" ||
        application.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [applications, search, statusFilter]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  };

  const openEdit = (application) => {
    setEditingId(application.id);
    setForm({
      companyName: application.companyName || "",
      jobTitle: application.jobTitle || "",
      jobDescription: application.jobDescription || "",
      applicationUrl: application.applicationUrl || "",
      appliedDate: application.appliedDate || "",
      interviewDate: application.interviewDate || "",
      status: application.status || "SAVED",
      notes: application.notes || "",
    });
    setShowForm(true);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!form.companyName.trim() || !form.jobTitle.trim()) {
      toast.error("Company name and job title are required.");
      return;
    }

    try {
      setSaving(true);
      if (editingId) {
        await updateApplication(editingId, form);
      } else {
        await createApplication(form);
      }
      toast.success(editingId ? "Application updated." : "Application added.");
      setShowForm(false);
      await loadData();
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Unable to save application."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this application?")) return;
    try {
      await deleteApplication(id);
      toast.success("Application deleted.");
      await loadData();
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Unable to delete application."
      );
    }
  };

  return (
    <div className="p-6 lg:p-8">
      <div className="flex flex-wrap items-start justify-between gap-6">
        <div>
          <h1 className="text-3xl font-bold text-white">
            Job Application Tracker
          </h1>
          <p className="mt-2 text-[#A7B8B5]">
            Keep every opportunity and next step in one place.
          </p>
        </div>
        <button
          onClick={openCreate}
          className="rounded-xl bg-[#408A71] px-6 py-3 font-semibold text-white"
        >
          Add application
        </button>
      </div>

      <Statistics statistics={statistics} />

      <div className="mt-8 flex flex-wrap gap-3">
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search company or job title"
          className="min-w-64 flex-1 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white outline-none"
        />
        <select
          value={statusFilter}
          onChange={(event) => setStatusFilter(event.target.value)}
          className="rounded-xl border border-white/10 bg-[#18352c] px-4 py-3 text-white"
        >
          <option value="ALL">All statuses</option>
          {STATUSES.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
      </div>

      {showForm && (
        <ApplicationForm
          form={form}
          editingId={editingId}
          saving={saving}
          onChange={updateField}
          onSubmit={handleSubmit}
          onCancel={() => setShowForm(false)}
        />
      )}

      <div className="mt-8 space-y-4">
        {loading && <p className="text-[#A7B8B5]">Loading applications...</p>}
        {!loading && !filteredApplications.length && (
          <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-10 text-center text-[#A7B8B5]">
            No applications match your filters.
          </div>
        )}
        {filteredApplications.map((application) => (
          <ApplicationCard
            key={application.id}
            application={application}
            onEdit={() => openEdit(application)}
            onDelete={() => handleDelete(application.id)}
          />
        ))}
      </div>
    </div>
  );
}

function Statistics({ statistics }) {
  if (!statistics) return null;
  return (
    <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <Stat label="Total" value={statistics.total} />
      <Stat label="Applied" value={statistics.byStatus?.APPLIED || 0} />
      <Stat label="Interviews" value={statistics.byStatus?.INTERVIEW || 0} />
      <Stat label="Offers" value={statistics.byStatus?.OFFER || 0} />
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
      <p className="text-sm text-[#A7B8B5]">{label}</p>
      <p className="mt-2 text-3xl font-bold text-[#B0E4CC]">{value}</p>
    </div>
  );
}

function ApplicationForm({
  form,
  editingId,
  saving,
  onChange,
  onSubmit,
  onCancel,
}) {
  return (
    <form
      onSubmit={onSubmit}
      className="mt-8 grid gap-4 rounded-2xl border border-white/10 bg-white/5 p-6 md:grid-cols-2"
    >
      <input required name="companyName" value={form.companyName} onChange={onChange} placeholder="Company name" className="field" />
      <input required name="jobTitle" value={form.jobTitle} onChange={onChange} placeholder="Job title" className="field" />
      <input name="applicationUrl" value={form.applicationUrl} onChange={onChange} placeholder="Application URL" className="field" />
      <select name="status" value={form.status} onChange={onChange} className="field">
        {STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
      </select>
      <label className="text-sm text-[#A7B8B5]">
        Applied date
        <input type="date" name="appliedDate" value={form.appliedDate} onChange={onChange} className="field mt-2 w-full" />
      </label>
      <label className="text-sm text-[#A7B8B5]">
        Interview date
        <input type="date" name="interviewDate" value={form.interviewDate} onChange={onChange} className="field mt-2 w-full" />
      </label>
      <textarea name="jobDescription" value={form.jobDescription} onChange={onChange} placeholder="Job description" className="field md:col-span-2" rows="3" />
      <textarea name="notes" value={form.notes} onChange={onChange} placeholder="Notes" className="field md:col-span-2" rows="3" />
      <div className="flex gap-3 md:col-span-2">
        <button disabled={saving} className="rounded-xl bg-[#408A71] px-5 py-3 font-semibold text-white disabled:opacity-50">
          {saving ? "Saving..." : editingId ? "Update application" : "Save application"}
        </button>
        <button type="button" onClick={onCancel} className="rounded-xl border border-white/10 px-5 py-3 text-white">
          Cancel
        </button>
      </div>
    </form>
  );
}

function ApplicationCard({ application, onEdit, onDelete }) {
  return (
    <article className="rounded-2xl border border-white/10 bg-white/5 p-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-white">{application.jobTitle}</h2>
          <p className="mt-1 text-[#B0E4CC]">{application.companyName}</p>
        </div>
        <span className="rounded-full bg-[#285A48] px-3 py-1 text-xs font-semibold text-white">
          {application.status}
        </span>
      </div>
      {application.notes && <p className="mt-4 text-[#D8E3DF]">{application.notes}</p>}
      <div className="mt-4 flex flex-wrap gap-4 text-sm text-[#A7B8B5]">
        {application.appliedDate && <span>Applied: {application.appliedDate}</span>}
        {application.interviewDate && <span>Interview: {application.interviewDate}</span>}
        {application.applicationUrl && (
          <a href={application.applicationUrl} target="_blank" rel="noreferrer" className="text-[#B0E4CC] underline">
            Open application
          </a>
        )}
      </div>
      <div className="mt-5 flex gap-3">
        <button onClick={onEdit} className="rounded-lg border border-white/10 px-4 py-2 text-sm text-white">Edit</button>
        <button onClick={onDelete} className="rounded-lg border border-red-400/30 px-4 py-2 text-sm text-red-300">Delete</button>
      </div>
    </article>
  );
}
