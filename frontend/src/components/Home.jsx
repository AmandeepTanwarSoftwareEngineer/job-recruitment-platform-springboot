import CandidateDashboard from "./CandidateDashboard";
import RecruiterDashboard from "./RecruiterDashboard";

// Shown after signing in. Recruiters get their company, jobs and received applications;
// job seekers get their profile, the job board and their applications.
export default function Home({ user, token, onLogout, onSessionExpired }) {
  const isRecruiter = user.role === "RECRUITER";

  return (
    <section>
      <p className="welcome">
        Welcome, <strong>{user.name}</strong>
      </p>
      <dl className="details">
        <dt>Email</dt>
        <dd>{user.email}</dd>
        <dt>Role</dt>
        <dd>{isRecruiter ? "Recruiter" : "Job seeker"}</dd>
      </dl>

      {isRecruiter ? (
        <RecruiterDashboard token={token} onSessionExpired={onSessionExpired} />
      ) : (
        <CandidateDashboard token={token} onSessionExpired={onSessionExpired} />
      )}

      <button type="button" className="btn secondary" onClick={onLogout}>
        Sign out
      </button>
    </section>
  );
}
