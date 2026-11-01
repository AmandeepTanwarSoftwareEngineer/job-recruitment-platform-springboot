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
2. Set environment variables `DB_PASSWORD` and `JWT_SECRET` (any random string of at least 32 characters, used to sign login tokens)
3. Run `JobportalApplication`