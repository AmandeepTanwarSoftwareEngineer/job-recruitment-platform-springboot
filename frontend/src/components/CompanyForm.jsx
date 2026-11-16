import { useState } from "react";
import { postJson } from "../api";
import Field from "./Field";

const EMPTY_FORM = { name: "", description: "", location: "" };

// POST /api/companies (recruiters only). The owner is not sent: the backend reads it from the token.
export default function CompanyForm({ token, onCreated, onSessionExpired }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const { ok, status, data } = await postJson("/api/companies", form, token);
    setLoading(false);
    if (ok) {
      onCreated(data); // CompanyResponse
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 409 "You already have a company", or 400 with fieldErrors
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}

      <Field
        label="Company name"
        name="name"
        type="text"
        maxLength={255}
        required
        value={form.name}
        onChange={handleChange}
        error={error?.fieldErrors?.name}
      />
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
        label="Description"
        name="description"
        type="text"
        maxLength={255}
        hint="Optional"
        value={form.description}
        onChange={handleChange}
        error={error?.fieldErrors?.description}
      />

      <button type="submit" className="btn" disabled={loading}>
        {loading ? "Creating company..." : "Create company"}
      </button>
    </form>
  );
}
