// Same values as the backend's ApplicationStatus enum.
export const APPLICATION_STATUSES = [
  { value: "APPLIED", label: "Applied" },
  { value: "REVIEWED", label: "Reviewed" },
  { value: "SHORTLISTED", label: "Shortlisted" },
  { value: "REJECTED", label: "Rejected" },
  { value: "HIRED", label: "Hired" },
];

export function statusLabel(value) {
  return APPLICATION_STATUSES.find((status) => status.value === value)?.label ?? value;
}

// The backend sends dates as LocalDateTime, e.g. "2026-11-16T10:20:30" (no time zone),
// which the browser reads as local time. Shows "16 Nov 2026".
export function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" });
}
