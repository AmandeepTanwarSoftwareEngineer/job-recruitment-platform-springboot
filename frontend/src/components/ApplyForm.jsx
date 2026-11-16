import { useState } from "react";
import { postJson } from "../api";
import Field from "./Field";

// POST /api/applications (job seekers with a profile). The candidate is not sent: the backend reads it from the token.
export default function ApplyForm({ token, job, onApplied, onCancel, onSessionExpired }) {
  const [coverLetter, setCoverLetter] = useState("");
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const { ok, status, data } = await postJson("/api/applications", { jobId: job.id, coverLetter }, token);
    setLoading(false);
    if (ok) {
      onApplied(data); // ApplicationResponse
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 409 "You have already applied to this job", 404 job deleted, 400 no profile
    }
  }

  return (
    <form className="form apply-form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}

      <Field
        label="Cover letter"
        name="coverLetter"
        multiline
        rows={4}
        maxLength={2000}
        hint="Optional. Why are you a good fit for this job?"
        value={coverLetter}
        onChange={(event) => setCoverLetter(event.target.value)}
        error={error?.fieldErrors?.coverLetter}
      />

      <div className="row">
        <button type="submit" className="btn" disabled={loading}>
          {loading ? "Applying..." : "Send application"}
        </button>
        <button type="button" className="btn secondary" onClick={onCancel} disabled={loading}>
          Cancel
        </button>
      </div>
    </form>
  );
}
