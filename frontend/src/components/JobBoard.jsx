import { useState } from "react";
import { jobTypeLabel } from "../jobTypes";
import ApplyForm from "./ApplyForm";
import StatusBadge from "./StatusBadge";

// Every job (GET /api/jobs) with an Apply button.
// statusByJobId: Map of jobId -> the status of the job seeker's application, for jobs they already applied to.
export default function JobBoard({ token, jobs, statusByJobId, hasProfile, onApplied, onSessionExpired }) {
  // Id of the job whose apply form is open (only one at a time), or null.
  const [applyingTo, setApplyingTo] = useState(null);

  if (jobs.length === 0) {
    return <p className="muted">No jobs posted yet. Ask a recruiter to post one, then press Refresh.</p>;
  }

  function handleApplied(application) {
    setApplyingTo(null);
    onApplied(application);
  }

  return (
    <ul className="job-list">
      {jobs.map((job) => {
        const appliedStatus = statusByJobId.get(job.id);
        return (
          <li key={job.id} className="job">
            <div className="job-head">
              <div>
                <strong>{job.title}</strong>
                <span className="muted block">{job.companyName}</span>
              </div>
              {appliedStatus ? (
                <StatusBadge status={appliedStatus} />
              ) : (
                applyingTo !== job.id && (
                  <button
                    type="button"
                    className="btn small"
                    disabled={!hasProfile}
                    title={hasProfile ? undefined : "Create your profile first"}
                    onClick={() => setApplyingTo(job.id)}
                  >
                    Apply
                  </button>
                )
              )}
            </div>

            <span className="muted">
              {[jobTypeLabel(job.jobType), job.location, job.salary].filter(Boolean).join(" · ")}
            </span>
            {job.description && <p className="job-description">{job.description}</p>}

            {applyingTo === job.id && (
              <ApplyForm
                token={token}
                job={job}
                onApplied={handleApplied}
                onCancel={() => setApplyingTo(null)}
                onSessionExpired={onSessionExpired}
              />
            )}
          </li>
        );
      })}
    </ul>
  );
}
