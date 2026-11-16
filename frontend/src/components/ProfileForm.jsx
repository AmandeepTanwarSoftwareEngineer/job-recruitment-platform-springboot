import { useState } from "react";
import { putJson } from "../api";
import Field from "./Field";

// The form keeps every value as text; null from the server becomes "".
function toForm(profile) {
  return {
    phone: profile?.phone ?? "",
    skills: profile?.skills ?? "",
    experienceYears: profile?.experienceYears ?? "",
    currentLocation: profile?.currentLocation ?? "",
    resumeUrl: profile?.resumeUrl ?? "",
  };
}

// PUT /api/candidates/me (job seekers only): creates the profile the first time, updates it after.
// The user is not sent: the backend reads it from the token.
export default function ProfileForm({ token, profile, onSaved, onCancel, onSessionExpired }) {
  const [form, setForm] = useState(() => toForm(profile));
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const body = {
      ...form,
      // An empty number box means "not given", so send null instead of "".
      experienceYears: form.experienceYears === "" ? null : Number(form.experienceYears),
    };
    const { ok, status, data } = await putJson("/api/candidates/me", body, token);
    setLoading(false);
    if (ok) {
      onSaved(data); // CandidateResponse
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 400 with fieldErrors
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}

      <Field
        label="Skills"
        name="skills"
        multiline
        rows={3}
        maxLength={1000}
        required
        hint="e.g. Java, Spring Boot, React, MySQL"
        value={form.skills}
        onChange={handleChange}
        error={error?.fieldErrors?.skills}
      />
      <Field
        label="Experience (years)"
        name="experienceYears"
        type="number"
        min={0}
        max={50}
        hint="Optional"
        value={form.experienceYears}
        onChange={handleChange}
        error={error?.fieldErrors?.experienceYears}
      />
      <Field
        label="Phone"
        name="phone"
        type="tel"
        maxLength={20}
        hint="Optional"
        value={form.phone}
        onChange={handleChange}
        error={error?.fieldErrors?.phone}
      />
      <Field
        label="Current location"
        name="currentLocation"
        type="text"
        maxLength={100}
        hint="Optional"
        value={form.currentLocation}
        onChange={handleChange}
        error={error?.fieldErrors?.currentLocation}
      />
      <Field
        label="Resume link"
        name="resumeUrl"
        type="url"
        maxLength={500}
        hint="Optional, e.g. a Google Drive link that starts with https://"
        value={form.resumeUrl}
        onChange={handleChange}
        error={error?.fieldErrors?.resumeUrl}
      />

      <div className="row">
        <button type="submit" className="btn" disabled={loading}>
          {loading ? "Saving..." : profile ? "Save changes" : "Create profile"}
        </button>
        {onCancel && (
          <button type="button" className="btn secondary" onClick={onCancel} disabled={loading}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
