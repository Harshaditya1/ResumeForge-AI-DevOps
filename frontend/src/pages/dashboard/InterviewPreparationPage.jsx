import { useCallback, useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  FaExternalLinkAlt,
  FaBookmark,
  FaRegBookmark,
  FaCheckCircle,
  FaRegCircle,
  FaSearch,
  FaChevronDown,
  FaChevronUp,
  FaBookOpen,
  FaLightbulb,
} from "react-icons/fa";
import {
  getInterviewProgress,
  getInterviewQuestions,
  updateInterviewProgress,
} from "../../services/interviewPreparationService";

const CATEGORIES = [
  { id: "ALL", label: "All Categories" },
  { id: "JAVA", label: "Java" },
  { id: "DSA", label: "DSA" },
  { id: "DBMS", label: "DBMS" },
  { id: "OPERATING_SYSTEMS", label: "Operating Systems" },
  { id: "COMPUTER_NETWORKS", label: "Computer Networks" },
  { id: "OOP", label: "OOP" },
  { id: "SPRING_BOOT", label: "Spring Boot" },
  { id: "HR", label: "HR" },
  { id: "BEHAVIORAL", label: "Behavioral" },
];

const DIFFICULTIES = [
  { id: "ALL", label: "All Difficulties" },
  { id: "EASY", label: "Easy", color: "text-emerald-400 bg-emerald-500/10 border-emerald-500/30" },
  { id: "MEDIUM", label: "Medium", color: "text-amber-400 bg-amber-500/10 border-amber-500/30" },
  { id: "HARD", label: "Hard", color: "text-rose-400 bg-rose-500/10 border-rose-500/30" },
];

export default function InterviewPreparationPage() {
  const [questions, setQuestions] = useState([]);
  const [progress, setProgress] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState("ALL");
  const [selectedDifficulty, setSelectedDifficulty] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [filterView, setFilterView] = useState("ALL"); // ALL, BOOKMARKED, COMPLETED
  const [expandedAnswers, setExpandedAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);

  const loadData = useCallback(async (searchVal = searchQuery) => {
    try {
      setLoading(true);
      const params = {};
      if (selectedCategory !== "ALL") params.category = selectedCategory;
      if (selectedDifficulty !== "ALL") params.difficulty = selectedDifficulty;
      if (searchVal && searchVal.trim()) params.search = searchVal.trim();
      if (filterView === "BOOKMARKED") params.bookmarkedOnly = true;

      const [questionData, progressData] = await Promise.all([
        getInterviewQuestions(params),
        getInterviewProgress(),
      ]);
      setQuestions(questionData || []);
      setProgress(progressData);
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Unable to load interview questions."
      );
    } finally {
      setLoading(false);
    }
  }, [selectedCategory, selectedDifficulty, searchQuery, filterView]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleToggleExpand = (id) => {
    setExpandedAnswers((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const handleToggleFavorite = async (question) => {
    try {
      setUpdatingId(question.id);
      const nextFavorite = !question.favorite;
      const res = await updateInterviewProgress(
        question.id,
        question.completed,
        nextFavorite
      );
      setQuestions((current) =>
        current.map((item) => (item.id === question.id ? res.data : item))
      );
      const updatedProgress = await getInterviewProgress();
      setProgress(updatedProgress);
      toast.success(nextFavorite ? "Saved to bookmarks." : "Removed from bookmarks.");
    } catch (error) {
      toast.error(error?.response?.data?.message || "Failed to update bookmark.");
    } finally {
      setUpdatingId(null);
    }
  };

  const handleToggleCompleted = async (question) => {
    try {
      setUpdatingId(question.id);
      const nextCompleted = !question.completed;
      const res = await updateInterviewProgress(
        question.id,
        nextCompleted,
        question.favorite
      );
      setQuestions((current) =>
        current.map((item) => (item.id === question.id ? res.data : item))
      );
      const updatedProgress = await getInterviewProgress();
      setProgress(updatedProgress);
      toast.success(nextCompleted ? "Marked as completed!" : "Marked as incomplete.");
    } catch (error) {
      toast.error(error?.response?.data?.message || "Failed to update progress.");
    } finally {
      setUpdatingId(null);
    }
  };

  const filteredQuestions = useMemo(() => {
    return questions.filter((q) => {
      if (filterView === "COMPLETED") return q.completed;
      if (filterView === "BOOKMARKED") return q.favorite;
      return true;
    });
  }, [questions, filterView]);

  const overallPercent = useMemo(() => {
    if (!progress || progress.totalQuestions === 0) return 0;
    return Math.round((progress.completedQuestions / progress.totalQuestions) * 100);
  }, [progress]);

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
              <FaBookOpen className="text-xl" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                Interview Preparation Hub
              </h1>
              <p className="text-sm text-[#A7B8B5] mt-0.5">
                Practice real curated interview questions with verified sources and full explanations.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Preparation Progress Card */}
      {progress && (
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-white/10">
            <div>
              <h2 className="text-base font-semibold text-white">Overall Preparation Progress</h2>
              <p className="text-xs text-[#A7B8B5] mt-1">
                Completed {progress.completedQuestions} of {progress.totalQuestions} questions ({overallPercent}%)
                {" · "}{progress.favoriteQuestions} Bookmarked
              </p>
            </div>
            <div className="w-full md:w-64">
              <div className="flex justify-between text-xs text-[#A7B8B5] mb-1.5">
                <span>Completion</span>
                <span className="font-semibold text-[#B0E4CC]">{overallPercent}%</span>
              </div>
              <div className="w-full h-2.5 rounded-full bg-black/40 overflow-hidden border border-white/5">
                <div
                  className="h-full bg-gradient-to-r from-[#2F80ED] to-[#408A71] transition-all duration-500 rounded-full"
                  style={{ width: `${overallPercent}%` }}
                />
              </div>
            </div>
          </div>

          {/* Category-wise Breakdown */}
          <div className="mt-5 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
            {progress.byCategory &&
              Object.entries(progress.byCategory).map(([catKey, catVal]) => {
                const catPercent = catVal.totalQuestions > 0
                  ? Math.round((catVal.completedQuestions / catVal.totalQuestions) * 100)
                  : 0;
                const formattedName = catKey.replaceAll("_", " ");
                const isSelected = selectedCategory === catKey;

                return (
                  <button
                    key={catKey}
                    onClick={() => setSelectedCategory(isSelected ? "ALL" : catKey)}
                    className={`text-left p-3 rounded-xl border transition-all ${
                      isSelected
                        ? "bg-[#408A71]/20 border-[#408A71] text-white shadow-sm"
                        : "bg-white/[0.03] border-white/5 text-[#D8E3DF] hover:bg-white/[0.07]"
                    }`}
                  >
                    <div className="flex items-center justify-between text-xs font-medium mb-1">
                      <span className="truncate pr-1">{formattedName}</span>
                      <span className="text-[#B0E4CC] font-bold">{catPercent}%</span>
                    </div>
                    <div className="w-full h-1.5 rounded-full bg-black/40 overflow-hidden">
                      <div
                        className="h-full bg-[#408A71] rounded-full transition-all"
                        style={{ width: `${catPercent}%` }}
                      />
                    </div>
                    <div className="mt-1.5 text-[10px] text-[#A7B8B5]">
                      {catVal.completedQuestions} / {catVal.totalQuestions} done
                    </div>
                  </button>
                );
              })}
          </div>
        </div>
      )}

      {/* Filters and Search Bar */}
      <div className="space-y-4">
        <div className="flex flex-col lg:flex-row gap-3">
          {/* Search Box */}
          <div className="relative flex-1">
            <FaSearch className="absolute left-4 top-1/2 -translate-y-1/2 text-[#A7B8B5]/60" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && loadData(e.target.value)}
              placeholder="Search by topic, question, answer, or source..."
              className="w-full pl-11 pr-4 py-3 rounded-xl border border-white/10 bg-white/5 text-white placeholder-[#A7B8B5]/50 outline-none focus:border-[#408A71] transition"
            />
          </div>

          {/* Difficulty Dropdown */}
          <select
            value={selectedDifficulty}
            onChange={(e) => setSelectedDifficulty(e.target.value)}
            className="px-4 py-3 rounded-xl border border-white/10 bg-[#18352c] text-white outline-none focus:border-[#408A71] transition"
          >
            {DIFFICULTIES.map((d) => (
              <option key={d.id} value={d.id}>
                {d.label}
              </option>
            ))}
          </select>

          {/* View Filter Buttons */}
          <div className="flex rounded-xl border border-white/10 bg-white/5 p-1 gap-1">
            <button
              onClick={() => setFilterView("ALL")}
              className={`px-3 py-2 rounded-lg text-xs font-semibold transition ${
                filterView === "ALL"
                  ? "bg-[#408A71] text-white"
                  : "text-[#A7B8B5] hover:text-white"
              }`}
            >
              All ({questions.length})
            </button>
            <button
              onClick={() => setFilterView("BOOKMARKED")}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold transition ${
                filterView === "BOOKMARKED"
                  ? "bg-[#408A71] text-white"
                  : "text-[#A7B8B5] hover:text-white"
              }`}
            >
              <FaBookmark className="text-[10px]" />
              Saved
            </button>
            <button
              onClick={() => setFilterView("COMPLETED")}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold transition ${
                filterView === "COMPLETED"
                  ? "bg-[#408A71] text-white"
                  : "text-[#A7B8B5] hover:text-white"
              }`}
            >
              <FaCheckCircle className="text-[10px]" />
              Completed
            </button>
          </div>
        </div>

        {/* Category Pills Slider */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-thin">
          {CATEGORIES.map((cat) => {
            const active = selectedCategory === cat.id;
            return (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`px-3.5 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition border ${
                  active
                    ? "bg-[#408A71] text-white border-[#408A71]"
                    : "bg-white/5 text-[#A7B8B5] border-white/10 hover:border-white/20 hover:text-white"
                }`}
              >
                {cat.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Questions List */}
      <div className="space-y-4">
        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((n) => (
              <div
                key={n}
                className="h-44 rounded-2xl border border-white/10 bg-white/5 animate-pulse"
              />
            ))}
          </div>
        ) : filteredQuestions.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-12 text-center">
            <FaBookOpen className="mx-auto text-4xl text-[#A7B8B5]/40 mb-3" />
            <h3 className="text-lg font-semibold text-white">No questions found</h3>
            <p className="text-sm text-[#A7B8B5] mt-1 max-w-md mx-auto">
              No interview questions match your current category, difficulty, or search query.
            </p>
            <button
              onClick={() => {
                setSelectedCategory("ALL");
                setSelectedDifficulty("ALL");
                setSearchQuery("");
                setFilterView("ALL");
              }}
              className="mt-4 px-4 py-2 rounded-xl bg-[#408A71] text-white text-xs font-semibold hover:bg-[#34715c] transition"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          filteredQuestions.map((q) => {
            const isExpanded = expandedAnswers[q.id];
            const diffStyle =
              DIFFICULTIES.find((d) => d.id === q.difficulty)?.color ||
              "text-emerald-400 bg-emerald-500/10 border-emerald-500/30";

            return (
              <article
                key={q.id}
                className={`rounded-2xl border transition-all duration-200 ${
                  q.completed
                    ? "border-emerald-500/30 bg-emerald-950/10"
                    : "border-white/10 bg-white/5 hover:border-white/20"
                } p-5 sm:p-6`}
              >
                {/* Card Top: Badges and Actions */}
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex flex-wrap items-center gap-2">
                    {/* Category */}
                    <span className="px-2.5 py-0.5 rounded-md text-xs font-semibold bg-[#408A71]/20 text-[#B0E4CC] border border-[#408A71]/30">
                      {q.category.replaceAll("_", " ")}
                    </span>

                    {/* Topic */}
                    {q.topic && (
                      <span className="px-2.5 py-0.5 rounded-md text-xs text-[#A7B8B5] bg-white/5 border border-white/10">
                        {q.topic}
                      </span>
                    )}

                    {/* Difficulty */}
                    <span
                      className={`px-2.5 py-0.5 rounded-md text-xs font-semibold border ${diffStyle}`}
                    >
                      {q.difficulty}
                    </span>

                    {/* Source Name Badge */}
                    {q.sourceName && (
                      <span className="text-xs text-[#A7B8B5] flex items-center gap-1">
                        via <span className="text-white font-medium">{q.sourceName}</span>
                      </span>
                    )}
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2">
                    {/* Favorite / Bookmark */}
                    <button
                      onClick={() => handleToggleFavorite(q)}
                      disabled={updatingId === q.id}
                      title={q.favorite ? "Remove from bookmarks" : "Save question"}
                      className={`p-2 rounded-lg border transition ${
                        q.favorite
                          ? "border-amber-500/40 bg-amber-500/10 text-amber-400 hover:bg-amber-500/20"
                          : "border-white/10 bg-white/5 text-[#A7B8B5] hover:text-white hover:border-white/20"
                      }`}
                    >
                      {q.favorite ? <FaBookmark /> : <FaRegBookmark />}
                    </button>

                    {/* Mark Completed */}
                    <button
                      onClick={() => handleToggleCompleted(q)}
                      disabled={updatingId === q.id}
                      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition ${
                        q.completed
                          ? "border-emerald-500/40 bg-emerald-500/20 text-emerald-300 hover:bg-emerald-500/30"
                          : "border-white/10 bg-white/5 text-[#A7B8B5] hover:text-white hover:border-white/20"
                      }`}
                    >
                      {q.completed ? (
                        <>
                          <FaCheckCircle className="text-emerald-400" />
                          <span>Done</span>
                        </>
                      ) : (
                        <>
                          <FaRegCircle />
                          <span>Mark Done</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>

                {/* Question Title & Description */}
                <div className="mt-4">
                  <h3 className="text-lg font-semibold text-white">
                    {q.title || q.question}
                  </h3>
                  {q.title && q.question && q.title !== q.question && (
                    <p className="mt-1.5 text-sm text-[#D8E3DF] leading-relaxed">
                      {q.question}
                    </p>
                  )}
                </div>

                {/* Explanation / Answer Drawer */}
                {isExpanded && q.answer && (
                  <div className="mt-4 p-4 rounded-xl border border-white/10 bg-black/30 space-y-2">
                    <div className="flex items-center gap-2 text-xs font-semibold text-[#B0E4CC]">
                      <FaLightbulb />
                      <span>Explanation & Key Concepts</span>
                    </div>
                    <div className="text-sm text-[#D8E3DF] leading-relaxed whitespace-pre-line font-mono text-xs sm:text-sm">
                      {q.answer}
                    </div>
                  </div>
                )}

                {/* Footer: View Original Question Link + Show Answer Toggle */}
                <div className="mt-4 pt-4 border-t border-white/10 flex flex-wrap items-center justify-between gap-3">
                  <button
                    onClick={() => handleToggleExpand(q.id)}
                    className="flex items-center gap-2 text-xs font-medium text-[#B0E4CC] hover:text-white transition"
                  >
                    <span>{isExpanded ? "Hide Explanation" : "View Explanation / Answer"}</span>
                    {isExpanded ? <FaChevronUp className="text-[10px]" /> : <FaChevronDown className="text-[10px]" />}
                  </button>

                  {q.sourceUrl && (
                    <a
                      href={q.sourceUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#408A71] hover:text-[#B0E4CC] transition"
                    >
                      <span>View Original Question</span>
                      <FaExternalLinkAlt className="text-[10px]" />
                    </a>
                  )}
                </div>
              </article>
            );
          })
        )}
      </div>
    </div>
  );
}
