import RecruiterDashboard from "./RecruiterDashboard";

// Shown after signing in. Recruiters also get their company and jobs.
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
        <div className="panel">
          <p className="muted">Only recruiters can create a company and post jobs.</p>
        </div>
      )}

      <button type="button" className="btn secondary" onClick={onLogout}>
        Sign out
      </button>
    </section>
  );
}
