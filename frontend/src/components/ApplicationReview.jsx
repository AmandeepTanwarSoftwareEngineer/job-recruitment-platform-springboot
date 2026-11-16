import { useState } from "react";
import { patchJson } from "../api";
import { APPLICATION_STATUSES, formatDate } from "../applicationStatuses";
import Field from "./Field";
import StatusBadge from "./StatusBadge";

// One application to the recruiter's job: the candidate's details, plus a form to change the status.
// PATCH /api/applications/{id}/status
export default function ApplicationReview({ token, application, onUpdated, onSessionExpired }) {
  const [form, setForm] = useState({
    status: application.status,
    recruiterNote: application.recruiterNote ?? "",
  });
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [saved, setSaved] = useState(false); // shows "Saved." after a 200
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
    setSaved(false);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setSaved(false);

    const { ok, status, data } = await patchJson(`/api/applications/${application.id}/status`, form, token);
    setLoading(false);
    if (ok) {
      onUpdated(data); // ApplicationResponse
      setSaved(true);
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 404 not one of your applications, 400 with fieldErrors
    }
  }

  const details = [
    application.candidateExperienceYears != null && `${application.candidateExperienceYears} yrs experience`,
    application.candidateLocation,
    application.candidatePhone,
  ].filter(Boolean);

  return (
    <li className="job">
      <div className="job-head">
        <div>
          <strong>{application.candidateName}</strong>
          <span className="muted block">
            for <strong>{application.jobTitle}</strong> · applied {formatDate(application.appliedAt)}
          </span>
        </div>
        <StatusBadge status={application.status} />
      </div>

      <span className="muted">
        <a href={`mailto:${application.candidateEmail}`}>{application.candidateEmail}</a>
        {details.length > 0 && ` · ${details.join(" · ")}`}
      </span>
      <p className="job-description">
        <span className="muted">Skills: </span>
        {application.candidateSkills}
      </p>
      {application.candidateResumeUrl && (
        <a href={application.candidateResumeUrl} target="_blank" rel="noreferrer">
          Open resume
        </a>
      )}
      {application.coverLetter && <blockquote className="quote">{application.coverLetter}</blockquote>}

      <form className="form review-form" onSubmit={handleSubmit}>
        {error && <p className="message error">{error.message}</p>}
        {saved && <p className="message success">Saved.</p>}

        <div className="field">
          <label htmlFor={`status-${application.id}`}>Status</label>
          <select id={`status-${application.id}`} name="status" value={form.status} onChange={handleChange}>
            {APPLICATION_STATUSES.map((status) => (
              <option key={status.value} value={status.value}>
                {status.label}
              </option>
            ))}
          </select>
          {error?.fieldErrors?.status && <small className="field-error">{error.fieldErrors.status}</small>}
        </div>
        <Field
          label="Private note"
          name="recruiterNote"
          multiline
          rows={2}
          maxLength={1000}
          hint="Only you see this, not the candidate"
          value={form.recruiterNote}
          onChange={handleChange}
          error={error?.fieldErrors?.recruiterNote}
        />

        <button type="submit" className="btn small" disabled={loading}>
          {loading ? "Saving..." : "Save"}
        </button>
      </form>
    </li>
  );
}
