import { statusLabel } from "../applicationStatuses";

// A small coloured pill for an application status, e.g. "Shortlisted".
export default function StatusBadge({ status }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{statusLabel(status)}</span>;
}
