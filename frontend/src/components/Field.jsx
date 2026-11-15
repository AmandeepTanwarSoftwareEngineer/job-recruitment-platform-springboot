import { useId } from "react";

// A labelled input, an optional hint, and the server's error for this field (from "fieldErrors").
export default function Field({ label, hint, error, ...inputProps }) {
  const id = useId();
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <input id={id} {...inputProps} />
      {hint && <small className="hint">{hint}</small>}
      {error && <small className="field-error">{error}</small>}
    </div>
  );
}
