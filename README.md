# Job & Recruitment Platform

A backend where Recruiters post jobs and Candidates discover and apply to them,
with JWT authentication, role-based access and application tracking.

## Tech Stack
Java 21 · Spring Boot · Spring Data JPA · MySQL · Spring Security + JWT · Maven

## Modules
- Auth – register, login (JWT)
- Company – recruiter company profile
- Job – post, edit, delete, search jobs
- Application – apply, track, update status

## Run locally
1. Start MySQL
2. Set environment variable `DB_PASSWORD`
3. Run `JobportalApplication`