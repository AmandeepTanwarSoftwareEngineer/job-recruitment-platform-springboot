import { useState } from "react";
import { postJson } from "../api";
import { JOB_TYPES } from "../jobTypes";
import Field from "./Field";

const EMPTY_FORM = { title: "", jobType: "FULL_TIME", location: "", salary: "", description: "" };

// POST /api/jobs (recruiters with a company). The company is not sent: the backend finds it from the token.
export default function JobForm({ token, onCreated, onSessionExpired }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [posted, setPosted] = useState(false); // shows "Job posted." after a 201
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
    setPosted(false);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setPosted(false);

    const { ok, status, data } = await postJson("/api/jobs", form, token);
    setLoading(false);
    if (ok) {
      onCreated(data); // JobResponse
      setForm(EMPTY_FORM);
      setPosted(true);
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 400 "Create your company before posting a job", or 400 with fieldErrors
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}
      {posted && <p className="message success">Job posted.</p>}

      <Field
        label="Job title"
        name="title"
        type="text"
        maxLength={150}
        required
        value={form.title}
        onChange={handleChange}
        error={error?.fieldErrors?.title}
      />

      <div className="field">
        <label htmlFor="job-type">Job type</label>
        <select id="job-type" name="jobType" value={form.jobType} onChange={handleChange}>
          {JOB_TYPES.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
        {error?.fieldErrors?.jobType && <small className="field-error">{error.fieldErrors.jobType}</small>}
      </div>

      <Field
        label="Location"
        name="location"
        type="text"
        maxLength={255}
        hint="Optional"
        value={form.location}
        onChange={handleChange}
        error={error?.fieldErrors?.location}
      />
      <Field
        label="Salary"
        name="salary"
        type="text"
        maxLength={50}
        hint="Optional, e.g. 6-8 LPA"
        value={form.salary}
        onChange={handleChange}
        error={error?.fieldErrors?.salary}
      />
      <Field
        label="Description"
        name="description"
        multiline
        rows={4}
        maxLength={2000}
        hint="Optional"
        value={form.description}
        onChange={handleChange}
        error={error?.fieldErrors?.description}
      />

      <button type="submit" className="btn" disabled={loading}>
        {loading ? "Posting job..." : "Post job"}
      </button>
    </form>
  );
}
