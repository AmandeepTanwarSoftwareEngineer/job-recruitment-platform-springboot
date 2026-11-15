// Shown after signing in.
export default function Home({ user, onLogout }) {
  return (
    <section>
      <p className="welcome">
        Welcome, <strong>{user.name}</strong>
      </p>
      <dl className="details">
        <dt>Email</dt>
        <dd>{user.email}</dd>
        <dt>Role</dt>
        <dd>{user.role === "RECRUITER" ? "Recruiter" : "Job seeker"}</dd>
      </dl>
      <button type="button" className="btn secondary" onClick={onLogout}>
        Sign out
      </button>
    </section>
  );
}
