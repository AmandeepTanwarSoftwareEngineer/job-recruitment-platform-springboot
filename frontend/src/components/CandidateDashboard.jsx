import { useEffect, useState } from "react";
import { getJson } from "../api";
import JobBoard from "./JobBoard";
import MyApplications from "./MyApplications";
import ProfileForm from "./ProfileForm";

// What a job seeker sees after signing in: their profile, every job (with Apply), and their applications.
export default function CandidateDashboard({ token, onSessionExpired }) {
  const [status, setStatus] = useState("loading"); // "loading" | "ready" | "error"
  const [error, setError] = useState("");
  const [profile, setProfile] = useState(null); // CandidateResponse, or null if not created yet
  const [jobs, setJobs] = useState([]); // JobResponse list
  const [applications, setApplications] = useState([]); // ApplicationResponse list
  const [editingProfile, setEditingProfile] = useState(false);
  // Bumped by the Refresh button to load everything again (e.g. after a recruiter posts a job or changes a status).
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let ignore = false; // drop the answer if the dashboard was closed before it arrived

    async function load() {
      const [profileResult, jobsResult, applicationsResult] = await Promise.all([
        getJson("/api/candidates/me", token),
        getJson("/api/jobs", token),
        getJson("/api/applications/mine", token),
      ]);
      if (ignore) return;

      const results = [profileResult, jobsResult, applicationsResult];
      if (results.some((result) => result.status === 401)) {
        onSessionExpired();
        return;
      }
      // A 404 from /candidates/me is not an error: it just means "no profile yet".
      const failed = results.find((result) => !result.ok && !(result === profileResult && result.status === 404));
      if (failed) {
        setError(failed.data.message);
        setStatus("error");
        return;
      }

      setProfile(profileResult.ok ? profileResult.data : null);
      setJobs(jobsResult.data);
      setApplications(applicationsResult.data);
      setStatus("ready");
    }

    load();
    return () => {
      ignore = true;
    };
    // Only reload when the token changes or Refresh is pressed; onSessionExpired is a new function on every App render.
  }, [token, reloadKey]);

  if (status === "loading") {
    return (
      <div className="panel">
        <p className="muted">Loading...</p>
      </div>
    );
  }

  if (status === "error") {
    return <p className="message error notice">{error}</p>;
  }

  function handleProfileSaved(saved) {
    setProfile(saved);
    setEditingProfile(false);
  }

  const statusByJobId = new Map(applications.map((application) => [application.jobId, application.status]));

  return (
    <>
      <div className="panel">
        <div className="panel-header">
          <h2 className="panel-title">Your profile</h2>
          {profile && !editingProfile && (
            <button type="button" className="btn secondary small" onClick={() => setEditingProfile(true)}>
              Edit
            </button>
          )}
        </div>

        {!profile ? (
          <>
            <p className="muted notice">Create your profile before applying. Recruiters see it with your application.</p>
            <ProfileForm token={token} profile={null} onSaved={handleProfileSaved} onSessionExpired={onSessionExpired} />
          </>
        ) : editingProfile ? (
          <ProfileForm
            token={token}
            profile={profile}
            onSaved={handleProfileSaved}
            onCancel={() => setEditingProfile(false)}
            onSessionExpired={onSessionExpired}
          />
        ) : (
          <dl className="details">
            <dt>Skills</dt>
            <dd>{profile.skills}</dd>
            <dt>Experience</dt>
            <dd>{profile.experienceYears != null ? `${profile.experienceYears} years` : "-"}</dd>
            <dt>Phone</dt>
            <dd>{profile.phone || "-"}</dd>
            <dt>Location</dt>
            <dd>{profile.currentLocation || "-"}</dd>
            <dt>Resume</dt>
            <dd>
              {profile.resumeUrl ? (
                <a href={profile.resumeUrl} target="_blank" rel="noreferrer">
                  Open resume
                </a>
              ) : (
                "-"
              )}
            </dd>
          </dl>
        )}
      </div>

      <div className="panel">
        <div className="panel-header">
          <h2 className="panel-title">Jobs ({jobs.length})</h2>
          <button type="button" className="btn secondary small" onClick={() => setReloadKey((key) => key + 1)}>
            Refresh
          </button>
        </div>
        <JobBoard
          token={token}
          jobs={jobs}
          statusByJobId={statusByJobId}
          hasProfile={profile !== null}
          onApplied={(application) => setApplications((current) => [application, ...current])}
          onSessionExpired={onSessionExpired}
        />
      </div>

      <div className="panel">
        <h2 className="panel-title">Your applications ({applications.length})</h2>
        <MyApplications applications={applications} />
      </div>
    </>
  );
}
