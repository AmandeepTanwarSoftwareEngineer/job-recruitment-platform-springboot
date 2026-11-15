import { useState } from "react";
import { postJson } from "../api";
import Field from "./Field";

// POST /api/auth/login
export default function LoginForm({ registeredEmail, onLogin }) {
  const [form, setForm] = useState({ email: registeredEmail, password: "" });
  const [error, setError] = useState(null); // ApiErrorResponse from the server
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const { ok, data } = await postJson("/api/auth/login", form);
    setLoading(false);
    if (ok) {
      onLogin(data);
    } else {
      setError(data); // e.g. 401 "Invalid email or password"
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      {error && <p className="message error">{error.message}</p>}
      {!error && registeredEmail && <p className="message success">Account created. Please sign in.</p>}

      <Field
        label="Email"
        name="email"
        type="email"
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
        autoComplete="current-password"
        required
        autoFocus={Boolean(registeredEmail)}
        value={form.password}
        onChange={handleChange}
        error={error?.fieldErrors?.password}
      />

      <button type="submit" className="btn" disabled={loading}>
        {loading ? "Signing in..." : "Sign in"}
      </button>
    </form>
  );
}
