import { useEffect, useState } from "react";
import { getJson } from "../api";
import CompanyForm from "./CompanyForm";
import JobForm from "./JobForm";
import JobList from "./JobList";
import ReceivedApplications from "./ReceivedApplications";

// What a recruiter sees after signing in:
// no company yet -> the create-company form;
// has a company -> company details, post-a-job form, job list, applications received.
export default function RecruiterDashboard({ token, onSessionExpired }) {
  const [status, setStatus] = useState("loading"); // "loading" | "ready" | "error"
  const [error, setError] = useState("");
  const [company, setCompany] = useState(null); // CompanyResponse, or null if not created yet
  const [jobs, setJobs] = useState([]); // JobResponse list

  // Load the company and its jobs once, when the dashboard opens.
  useEffect(() => {
    let ignore = false; // drop the answer if the dashboard was closed before it arrived

    async function load() {
      const [companyResult, jobsResult] = await Promise.all([
        getJson("/api/companies/me", token),
        getJson("/api/jobs/mine", token),
      ]);
      if (ignore) return;

      if (companyResult.status === 401 || jobsResult.status === 401) {
        onSessionExpired();
        return;
      }
      // A 404 from /companies/me is not an error: it just means "no company yet".
      const companyFailed = !companyResult.ok && companyResult.status !== 404;
      if (companyFailed || !jobsResult.ok) {
        setError((companyFailed ? companyResult : jobsResult).data.message);
        setStatus("error");
        return;
      }

      setCompany(companyResult.ok ? companyResult.data : null);
      setJobs(jobsResult.data);
      setStatus("ready");
    }

    load();
    return () => {
      ignore = true;
    };
    // Only reload when the token changes; onSessionExpired is a new function on every App render.
  }, [token]);

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

  if (!company) {
    return (
      <div className="panel">
        <h2 className="panel-title">Create your company</h2>
        <p className="muted notice">You need a company before you can post jobs.</p>
        <CompanyForm token={token} onCreated={setCompany} onSessionExpired={onSessionExpired} />
      </div>
    );
  }

  return (
    <>
      <div className="panel">
        <h2 className="panel-title">Your company</h2>
        <dl className="details">
          <dt>Name</dt>
          <dd>{company.name}</dd>
          <dt>Location</dt>
          <dd>{company.location || "-"}</dd>
          <dt>About</dt>
          <dd>{company.description || "-"}</dd>
        </dl>
      </div>

      <div className="panel">
        <h2 className="panel-title">Post a job</h2>
        <JobForm
          token={token}
          onCreated={(job) => setJobs((current) => [job, ...current])}
          onSessionExpired={onSessionExpired}
        />
      </div>

      <div className="panel">
        <h2 className="panel-title">Your jobs ({jobs.length})</h2>
        <JobList jobs={jobs} />
      </div>

      <div className="panel">
        <ReceivedApplications token={token} onSessionExpired={onSessionExpired} />
      </div>
    </>
  );
}
