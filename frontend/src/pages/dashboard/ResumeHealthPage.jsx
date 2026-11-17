import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import {
  getLatestResumeHealth,
  updateResumeHealthChecklist,
} from "../../services/resumeHealthService";

export default function ResumeHealthPage() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [savingKey, setSavingKey] = useState(null);

  useEffect(() => {
    getLatestResumeHealth()
      .then(setHealth)
      .catch((error) => {
        toast.error(
          error?.response?.data?.message ||
            "Upload a resume to calculate its health."
        );
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
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Unable to update the improvement checklist."
      );
    } finally {
      setSavingKey(null);
    }
  };

  if (loading) {
    return <p className="p-8 text-[#A7B8B5]">Calculating resume health...</p>;
  }

  if (!health) {
    return (
      <div className="p-8">
        <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-10 text-center text-[#A7B8B5]">
          Upload a resume to see its health report.
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 lg:p-8">
      <h1 className="text-3xl font-bold text-white">
        Resume Health Center
      </h1>
      <p className="mt-2 text-[#A7B8B5]">
        Evidence-based checks from {health.fileName}.
      </p>

      <div className="mt-8 grid gap-6 lg:grid-cols-[240px_1fr]">
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 text-center">
          <p className="text-sm text-[#A7B8B5]">Resume health score</p>
          <p className="mt-4 text-6xl font-bold text-[#B0E4CC]">
            {health.score}
          </p>
          <p className="mt-2 text-[#A7B8B5]">
            {health.completedSections} of {health.totalSections} sections detected
          </p>
          <p className="mt-4 text-sm text-[#A7B8B5]">
            Checklist: {health.checklistCompleted} of {health.checklistTotal}
          </p>
        </div>

        <div className="space-y-4">
          {health.sections.map((section) => (
            <section
              key={section.key}
              className="rounded-2xl border border-white/10 bg-white/5 p-5"
            >
              <div className="flex items-center justify-between gap-4">
                <h2 className="font-semibold text-white">{section.label}</h2>
                <span
                  className={
                    section.complete
                      ? "text-emerald-300"
                      : "text-amber-300"
                  }
                >
                  {section.complete ? "Detected" : "Needs attention"}
                </span>
              </div>
              <p className="mt-2 text-sm text-[#A7B8B5]">
                {section.recommendation}
              </p>
              <label className="mt-4 flex cursor-pointer items-center gap-3 text-sm text-[#D8E3DF]">
                <input
                  type="checkbox"
                  checked={section.checklistCompleted}
                  disabled={savingKey === section.key}
                  onChange={() => toggleChecklist(section)}
                />
                Mark improvement as complete
              </label>
            </section>
          ))}
        </div>
      </div>
    </div>
  );
}
