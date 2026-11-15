import { useState } from "react";
import { postJson } from "../api";
import Field from "./Field";

// POST /api/auth/register
export default function RegisterForm({ onRegistered }) {
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "JOB_SEEKER" });
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const { ok, data } = await postJson("/api/auth/register", form);
    setLoading(false);
    if (ok) {
      // Register does not return a token, so the user signs in next.
      onRegistered(data.user.email);
    } else {
      setError(data); // e.g. 409 "Email already exists", or 400 with fieldErrors
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}

      <Field
        label="Name"
        name="name"
        type="text"
        maxLength={100}
        autoComplete="name"
        required
        value={form.name}
        onChange={handleChange}
        error={error?.fieldErrors?.name}
      />
      <Field
        label="Email"
        name="email"
        type="email"
        maxLength={150}
        autoComplete="email"
        required
        value={form.email}
        onChange={handleChange}
        error={error?.fieldErrors?.email}
      />
      <Field
        label="Password"
        name="password"
        type="password"
        minLength={8}
        maxLength={72}
        autoComplete="new-password"
        required
        hint="At least 8 characters"
        value={form.password}
        onChange={handleChange}
        error={error?.fieldErrors?.password}
      />

      <div className="field">
        <label htmlFor="register-role">I am a</label>
        <select id="register-role" name="role" value={form.role} onChange={handleChange}>
          <option value="JOB_SEEKER">Job seeker</option>
          <option value="RECRUITER">Recruiter</option>
        </select>
      </div>

      <button type="submit" className="btn" disabled={loading}>
        {loading ? "Creating account..." : "Create account"}
      </button>
    </form>
  );
}
