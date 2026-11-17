import { useCallback, useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  getInterviewProgress,
  getInterviewQuestions,
  updateInterviewProgress,
} from "../../services/interviewPreparationService";

const CATEGORIES = [
  "JAVA",
  "DSA",
  "DBMS",
  "OPERATING_SYSTEMS",
  "COMPUTER_NETWORKS",
  "OOP",
  "SPRING_BOOT",
  "HR",
  "BEHAVIORAL",
];

export default function InterviewPreparationPage() {
  const [questions, setQuestions] = useState([]);
  const [progress, setProgress] = useState(null);
  const [category, setCategory] = useState("");
  const [difficulty, setDifficulty] = useState("");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);

  const loadQuestions = useCallback(async (searchValue = "") => {
    try {
      setLoading(true);
      const [questionData, progressData] = await Promise.all([
        getInterviewQuestions({
          ...(category && { category }),
          ...(difficulty && { difficulty }),
          ...(searchValue.trim() && { search: searchValue.trim() }),
        }),
        getInterviewProgress(),
      ]);
      setQuestions(questionData);
      setProgress(progressData);
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Unable to load interview preparation."
      );
    } finally {
      setLoading(false);
    }
  }, [category, difficulty]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadQuestions();
    }, 0);
    return () => clearTimeout(timer);
  }, [category, difficulty, loadQuestions]);

  const visibleQuestions = useMemo(
    () => questions.filter((question) => question.category),
    [questions]
  );

  const handleProgress = async (question) => {
    try {
      const response = await updateInterviewProgress(
        question.id,
        !question.completed,
        question.favorite
      );
      setQuestions((current) =>
        current.map((item) => (item.id === question.id ? response.data : item))
      );
      const nextProgress = await getInterviewProgress();
      setProgress(nextProgress);
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Unable to update progress."
      );
    }
  };

  const handleFavorite = async (question) => {
    try {
      const response = await updateInterviewProgress(
        question.id,
        question.completed,
        !question.favorite
      );
      setQuestions((current) =>
        current.map((item) => (item.id === question.id ? response.data : item))
      );
      const nextProgress = await getInterviewProgress();
      setProgress(nextProgress);
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Unable to update favorite."
      );
    }
  };

  return (
    <div className="p-6 lg:p-8">
      <h1 className="text-3xl font-bold text-white">
        Interview Preparation Hub
      </h1>
      <p className="mt-2 text-[#A7B8B5]">
        Practice curated questions, save favorites, and track your progress.
      </p>

      <div className="mt-8 grid gap-3 md:grid-cols-3">
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          onKeyDown={(event) =>
            event.key === "Enter" && loadQuestions(event.currentTarget.value)
          }
          placeholder="Search questions"
          className="rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white outline-none"
        />
        <select
          value={category}
          onChange={(event) => setCategory(event.target.value)}
          className="rounded-xl border border-white/10 bg-[#18352c] px-4 py-3 text-white"
        >
          <option value="">All categories</option>
          {CATEGORIES.map((item) => (
            <option key={item} value={item}>
              {item.replaceAll("_", " ")}
            </option>
          ))}
        </select>
        <select
          value={difficulty}
          onChange={(event) => setDifficulty(event.target.value)}
          className="rounded-xl border border-white/10 bg-[#18352c] px-4 py-3 text-white"
        >
          <option value="">All difficulties</option>
          <option value="EASY">Easy</option>
          <option value="MEDIUM">Medium</option>
          <option value="HARD">Hard</option>
        </select>
      </div>
      <button
        onClick={() => loadQuestions(search)}
        className="mt-3 rounded-xl border border-[#408A71] px-4 py-2 text-sm text-[#B0E4CC]"
      >
        Search
      </button>

      {progress && (
        <div className="mt-6 rounded-2xl border border-white/10 bg-white/5 p-5 text-[#D8E3DF]">
          Completed {progress.completedQuestions} of {progress.totalQuestions}
          {" · "}Favorites {progress.favoriteQuestions}
          <div className="mt-4 grid gap-2 sm:grid-cols-3 lg:grid-cols-5">
            {Object.entries(progress.byCategory || {}).map(
              ([key, value]) => (
                <span key={key} className="text-xs text-[#A7B8B5]">
                  {key.replaceAll("_", " ")}: {value.completedQuestions}/
                  {value.totalQuestions}
                </span>
              )
            )}
          </div>
        </div>
      )}

      <div className="mt-6 space-y-4">
        {loading && <p className="text-[#A7B8B5]">Loading questions...</p>}
        {!loading && !visibleQuestions.length && (
          <p className="text-[#A7B8B5]">No questions match your filters.</p>
        )}
        {visibleQuestions.map((question) => (
          <article
            key={question.id}
            className="rounded-2xl border border-white/10 bg-white/5 p-5"
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <span className="text-xs uppercase tracking-wide text-[#B0E4CC]">
                {question.category.replaceAll("_", " ")} · {question.difficulty}
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => handleFavorite(question)}
                  className="rounded-lg border border-white/10 px-3 py-1 text-sm text-white"
                >
                  {question.favorite ? "★ Saved" : "☆ Save"}
                </button>
                <button
                  onClick={() => handleProgress(question)}
                  className="rounded-lg bg-[#408A71] px-3 py-1 text-sm text-white"
                >
                  {question.completed ? "Completed" : "Mark complete"}
                </button>
              </div>
            </div>
            <h2 className="mt-3 font-semibold text-white">{question.question}</h2>
            <p className="mt-2 text-sm leading-6 text-[#A7B8B5]">
              {question.answer}
            </p>
          </article>
        ))}
      </div>
    </div>
  );
}
