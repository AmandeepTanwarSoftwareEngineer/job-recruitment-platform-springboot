import CompanyForm from "./CompanyForm";

// Shown after signing in. Recruiters also get the create-company form.
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

      <div className="panel">
        <h2 className="panel-title">Your company</h2>
        {isRecruiter ? (
          <CompanyForm token={token} onSessionExpired={onSessionExpired} />
        ) : (
          <p className="muted">Only recruiters can create a company.</p>
        )}
      </div>

      <button type="button" className="btn secondary" onClick={onLogout}>
        Sign out
      </button>
    </section>
  );
}
