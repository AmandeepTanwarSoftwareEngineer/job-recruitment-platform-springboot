import { formatDate } from "../applicationStatuses";
import StatusBadge from "./StatusBadge";

// The job seeker's applications (GET /api/applications/mine), newest first.
export default function MyApplications({ applications }) {
  if (applications.length === 0) {
    return <p className="muted">You have not applied to any job yet.</p>;
  }

  return (
    <ul className="job-list">
      {applications.map((application) => (
        <li key={application.id} className="job">
          <div className="job-head">
            <div>
              <strong>{application.jobTitle}</strong>
              <span className="muted block">{application.companyName}</span>
            </div>
            <StatusBadge status={application.status} />
          </div>
          <span className="muted">
            Applied {formatDate(application.appliedAt)}
            {application.reviewedAt && ` · Updated ${formatDate(application.reviewedAt)}`}
          </span>
        </li>
      ))}
    </ul>
  );
}
