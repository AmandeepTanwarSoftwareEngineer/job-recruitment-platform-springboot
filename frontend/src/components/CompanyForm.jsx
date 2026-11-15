import { useState } from "react";
import { postJson } from "../api";
import Field from "./Field";

const EMPTY_FORM = { name: "", description: "", location: "" };

// POST /api/companies (recruiters only). The owner is not sent: the backend reads it from the token.
export default function CompanyForm({ token, onSessionExpired }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [company, setCompany] = useState(null); // CompanyResponse after a 201
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
      setCompany(data);
      setForm(EMPTY_FORM);
    } else if (status === 401) {
      onSessionExpired(); // token expired or invalid: sign in again
    } else {
      setError(data); // e.g. 409 "You already have a company", or 400 with fieldErrors
    }
  }

  if (company) {
    return (
      <div className="form">
        <p className="message success">Company created.</p>
        <dl className="details">
          <dt>Name</dt>
          <dd>{company.name}</dd>
          <dt>Location</dt>
          <dd>{company.location || "-"}</dd>
          <dt>About</dt>
          <dd>{company.description || "-"}</dd>
        </dl>
      </div>
    );
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
