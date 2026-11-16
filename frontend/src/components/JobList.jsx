import { jobTypeLabel } from "../jobTypes";

// The recruiter's posted jobs (JobResponse list), newest first.
export default function JobList({ jobs }) {
  if (jobs.length === 0) {
    return <p className="muted">No jobs posted yet.</p>;
  }

  return (
    <ul className="job-list">
      {jobs.map((job) => (
        <li key={job.id} className="job">
          <strong>{job.title}</strong>
          <span className="muted">
            {[jobTypeLabel(job.jobType), job.location, job.salary].filter(Boolean).join(" · ")}
          </span>
        </li>
      ))}
    </ul>
  );
}
