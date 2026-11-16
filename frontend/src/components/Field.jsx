import { useId } from "react";

// A labelled input (or a textarea with multiline), an optional hint,
// and the server's error for this field (from "fieldErrors").
export default function Field({ label, hint, error, multiline, ...inputProps }) {
  const id = useId();
  const Input = multiline ? "textarea" : "input";
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <Input id={id} {...inputProps} />
      {hint && <small className="hint">{hint}</small>}
      {error && <small className="field-error">{error}</small>}
    </div>
  );
}
