import { useEffect, useState } from "react";
import { getJson } from "../api";
import ApplicationReview from "./ApplicationReview";

// Applications to the recruiter's jobs (GET /api/applications/received), newest first.
export default function ReceivedApplications({ token, onSessionExpired }) {
  const [status, setStatus] = useState("loading"); // "loading" | "ready" | "error"
  const [error, setError] = useState("");
  const [applications, setApplications] = useState([]); // ApplicationResponse list
  // Bumped by the Refresh button to load again (e.g. after a job seeker applies).
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let ignore = false; // drop the answer if the panel was closed before it arrived

    async function load() {
      const { ok, status: httpStatus, data } = await getJson("/api/applications/received", token);
      if (ignore) return;

      if (httpStatus === 401) {
        onSessionExpired();
        return;
      }
      if (!ok) {
        setError(data.message);
        setStatus("error");
        return;
      }
      setApplications(data);
      setStatus("ready");
    }

    load();
    return () => {
      ignore = true;
    };
    // Only reload when the token changes or Refresh is pressed; onSessionExpired is a new function on every App render.
  }, [token, reloadKey]);

  function handleUpdated(updated) {
    setApplications((current) => current.map((application) => (application.id === updated.id ? updated : application)));
  }

  return (
    <>
      <div className="panel-header">
        <h2 className="panel-title">Applications received ({applications.length})</h2>
        <button type="button" className="btn secondary small" onClick={() => setReloadKey((key) => key + 1)}>
          Refresh
        </button>
      </div>

      {status === "loading" && <p className="muted">Loading...</p>}
      {status === "error" && <p className="message error">{error}</p>}
      {status === "ready" && applications.length === 0 && <p className="muted">No applications yet.</p>}
      {status === "ready" && applications.length > 0 && (
        <ul className="job-list">
          {applications.map((application) => (
            <ApplicationReview
              key={application.id}
              token={token}
              application={application}
              onUpdated={handleUpdated}
              onSessionExpired={onSessionExpired}
            />
          ))}
        </ul>
      )}
    </>
  );
}
