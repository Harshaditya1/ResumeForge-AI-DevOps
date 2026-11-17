import { useEffect, useState } from "react";
import { getAnalysisHistory } from "../../services/analysisService";

export default function HistoryPage() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAnalysisHistory()
      .then(setHistory)
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6 lg:p-8">
      <h1 className="text-3xl font-bold text-white">Resume History</h1>
      <p className="mt-2 text-[#A7B8B5]">Review previous ATS analyses.</p>
      <div className="mt-8 space-y-4">
        {loading && <p className="text-[#A7B8B5]">Loading history...</p>}
        {!loading && !history.length && (
          <p className="rounded-2xl border border-white/10 bg-white/5 p-6 text-[#A7B8B5]">
            No analysis history yet.
          </p>
        )}
        {history.map((item, index) => (
          <div
            key={item.id || index}
            className="rounded-2xl border border-white/10 bg-white/5 p-6 text-[#D8E3DF]"
          >
            <span className="text-2xl font-bold text-[#B0E4CC]">
              {item.matchPercentage}%
            </span>
            <span className="ml-3 text-[#A7B8B5]">ATS match</span>
          </div>
        ))}
      </div>
    </div>
  );
}
