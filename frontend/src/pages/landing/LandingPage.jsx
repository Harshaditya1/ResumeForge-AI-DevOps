import { Link } from "react-router-dom";

export default function LandingPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[#091413] px-6 text-white">
      <section className="max-w-2xl text-center">
        <p className="text-sm font-semibold uppercase tracking-[0.3em] text-[#B0E4CC]">
          ResumeForge AI
        </p>
        <h1 className="mt-4 text-5xl font-bold">
          Build resumes that get noticed.
        </h1>
        <p className="mt-6 text-lg text-[#A7B8B5]">
          Upload your resume, compare it with a job description, and get
          actionable ATS and AI guidance.
        </p>
        <div className="mt-8 flex justify-center gap-4">
          <Link
            to="/register"
            className="rounded-xl bg-[#408A71] px-6 py-3 font-semibold hover:bg-[#4c9a7e]"
          >
            Create account
          </Link>
          <Link
            to="/login"
            className="rounded-xl border border-[#B0E4CC]/30 px-6 py-3 font-semibold hover:bg-white/10"
          >
            Sign in
          </Link>
        </div>
      </section>
    </main>
  );
}