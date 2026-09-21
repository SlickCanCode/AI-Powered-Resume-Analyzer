# 📄 Resume Analyzer (ResuMatch)

A **Java Backend, AI‑powered Resume Analyzer System** designed to help job seekers improve their resumes and better align them with specific job roles. This project demonstrates strong backend engineering, clean API design, thoughtful documentation, and practical AI integration.

---

## 🚀 Project Overview

The Resume Analyzer automatically parses resumes, analyzes content against job descriptions, and provides intelligent feedback such as:

* Resume quality scoring
* Skill and keyword matching
* AI‑generated improvement suggestions
* Job role compatibility insights

The goal of this project is to simulate a **real‑world recruitment support tool** while showcasing production‑ready backend development and system design.

---

## 🧠 Key Features

* **Resume Parsing**

  * Extracts structured data from uploaded resumes (PDF, Word documents, and images, up to 10 MB)
  * Identifies skills, experience, education, and online profiles

* **AI‑Powered Analysis**

  * Analyzes a resume on its own or against a job description
  * **Job match by URL:** paste a job posting link and the app extracts the posting and scores your resume against it
  * Generates actionable improvement suggestions and flags grammar issues
  * Provides a match score based on relevance

* **Resilient AI Layer**

  * Uses **Google Gemini** as the primary model
  * Automatically falls back to **OpenAI** when Gemini quota is exhausted or unavailable

* **Accounts & Subscriptions**

  * Email + OTP verification (sent via Resend) and Google OAuth2 sign‑in
  * Monthly analysis limits by plan: **Free (15)** and **Pro (25)**
  * Dashboard endpoints for usage stats and recent analyses

* **Secure Backend Architecture**

  * JWT authentication (access and refresh tokens delivered as cookies)
  * Clean RESTful API design
  * Input validation and centralized error handling

---

## 🛠️ Tech Stack

### Backend

* **Java 17**
* **Spring Boot 3.4** (REST APIs, Spring Security, OAuth2 Client)
* **JWT Authentication** (`java-jwt`)
* **Hibernate / JPA** with **Flyway** database migrations
* **PostgreSQL** (production) / **H2** (dev profile)

### AI & Processing

* **Google Gemini API** (primary) and **OpenAI via Spring AI** (fallback)
* **Apache PDFBox & Apache Tika** for document parsing
* **jsoup & OkHttp** for extracting job postings from URLs

### Infrastructure & Tooling

* **Docker** (multi‑stage build)
* **GitHub Actions** CI (`mvn clean verify`)
* **Testcontainers** (PostgreSQL) for integration tests
* **Resend** for transactional email

### Frontend (Planned / In Progress)

* **React**
* **Modern UI for resume upload & results visualization**

---

## 📐 System Design Highlights

* Layered architecture (Controller → Service → Repository)
* DTO‑based request/response handling
* Centralized exception handling (`ApplicationExceptionHandler`)
* Clean separation of AI logic from business logic: an `AiModelRouter` picks between the Gemini and OpenAI services behind a common `AiService` interface
* Versioned schema changes through Flyway migrations
* Easily extendable for future features (recruiter dashboard, analytics, etc.)

For a deeper walkthrough, see [`resume-analyzer/ARCHITECTURE.md`](resume-analyzer/ARCHITECTURE.md).

---

## 📄 API Documentation

The API follows REST conventions and is versioned under `/api/v1`. Everything except `/api/v1/auth/**` and user registration requires authentication.

Typical flow:

1. User registers and verifies their email with an OTP (or signs in with Google)
2. User uploads a resume, which is parsed and validated
3. AI analysis is triggered, optionally against a job description or job posting URL
4. Structured feedback and scores are returned

| Area | Method & Path | Description |
| --- | --- | --- |
| **Auth** | `POST /api/v1/auth/send-otp` | Send a one‑time code to an email address |
| | `POST /api/v1/auth/verify-otp` | Verify the code and receive auth cookies |
| | `GET /api/v1/auth/refresh` | Get a new access token using the refresh token |
| | `PATCH /api/v1/auth/reset-password` | Reset a password |
| | `POST /api/v1/auth/logout` | Clear auth cookies |
| **Users** | `POST /api/v1/users` | Register a new user |
| | `GET /api/v1/users/me` | Get the current user |
| | `PUT /api/v1/users/me` | Update the current user |
| | `PATCH /api/v1/users/me/change-password` | Change password |
| | `GET /api/v1/users/me/subscription` | Get plan and monthly usage |
| | `DELETE /api/v1/users/me` | Delete the account |
| **Resumes** | `POST /api/v1/resumes/upload` | Upload and parse a resume (`file` multipart field) |
| | `GET /api/v1/resumes` | List the user's resumes |
| | `GET /api/v1/resumes/{id}` | Get parsed resume data |
| | `DELETE /api/v1/resumes/{id}` | Delete a resume |
| **Analysis** | `POST /api/v1/resumes/{id}/analyze` | Analyze a resume (optionally against a job description) |
| | `POST /api/v1/resumes/{id}/analyze/job-match` | Match a resume against a job posting (`jobLink`) |
| | `GET /api/v1/resumes/analyses` | List all analyses for the user |
| | `GET /api/v1/resumes/{id}/analyses` | Get the analysis for a resume |
| | `DELETE /api/v1/resumes/{id}/analyses` | Delete a resume's analysis |
| **Dashboard** | `GET /api/v1/dashboard/me/stats` | Usage and score statistics |
| | `GET /api/v1/dashboard/me/recent-analyses` | Most recent analyses |

> Request and response shapes are defined by the DTOs in the `requests` and `reponses` packages and the controllers in `web`.

---

## 📦 Installation & Setup

### Prerequisites

* Java 17+
* PostgreSQL (for the default `prod` profile), or use the in‑memory H2 `dev` profile
* A Gemini API key and an OpenAI API key

### Run locally

```bash
# Clone the repository
git clone https://github.com/SlickCanCode/AI-Powered-Resume-Analyzer.git

# Navigate to the Spring Boot project
cd AI-Powered-Resume-Analyzer/resume-analyzer

# Run the application with the in-memory H2 dev profile
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

> The default active profile is `prod`, which expects a PostgreSQL database (see below). Use `dev` for a quick local start.

### Environment variables

```env
# AI providers
GEMINI_API_KEY=your_gemini_key
OPENAI_API_KEY=your_openai_key

# Auth
JWT_SECRET=your_long_random_secret
GOOGLE_CLIENT_ID=your_google_oauth_client_id
GOOGLE_CLIENT_SECRET=your_google_oauth_client_secret

# Email (OTP delivery)
RESEND_API_KEY=your_resend_key
RESUMATCH_EMAIL=sender_address_for_otp_emails

# Optional
FRONTEND_URL=http://localhost:3000   # default
PORT=8080                            # default

# Production database (prod profile)
PGURL=jdbc:postgresql://host:5432/dbname
PGUSER=your_db_user
PGPASSWORD=your_db_password
```

> ⚠️ Always set a strong `JWT_SECRET`. If it is missing, the app falls back to an insecure default that must never be used in production.

### Run with Docker

```bash
cd resume-analyzer
docker build -t resume-analyzer .
docker run -p 8080:8080 --env-file .env resume-analyzer
```

See [`resume-analyzer/DEPLOYMENT_GUIDE.md`](resume-analyzer/DEPLOYMENT_GUIDE.md) for the production deployment checklist.

---

## 🧪 Testing

```bash
cd resume-analyzer
./mvnw clean verify
```

* Unit tests for core services (resume, user, job posting extraction)
* Web layer tests for the resume controller
* Repository and end‑to‑end API workflow integration tests using **Testcontainers** (requires Docker)
* Validations for malformed or unsupported resume files
* Runs automatically on every push and pull request through **GitHub Actions**

---

## 📈 Future Improvements

* Advanced recruiter dashboard
* Resume history & version comparison
* Frontend UI polish and animations
* More detailed analytics & insights
* Multi‑language resume support

---

## 🤝 Contributions

Contributions are **highly welcome**, especially in the following areas:

* 🎨 **Frontend development (React UI/UX)**
* 📊 Data visualization for resume analysis results
* 🧪 Additional test coverage
* 📘 Documentation improvements

If you’re interested, feel free to fork the repo, open an issue, or submit a pull request.

---

## 📬 Contact

If you’d like to discuss this project, collaborate, or provide feedback:

* **Email:** [oreofeadelanwa3@gmail.com](mailto:oreofeadelanwa3@gmail.com)
* **LinkedIn:** [Oreofe Adelanwa](https://www.linkedin.com/in/oreofe-adelanwa-4aa04b368/)
* **Portfolio:** [instantfind.me/oreofeadelanwa](https://instantfind.me/oreofeadelanwa)

---

## ⭐ Final Note

This project is part of my developer portfolio and reflects my approach to:

* Writing clean, maintainable code
* Designing scalable backend systems
* Documenting projects professionally
* Building practical, real‑world software

If you find this project useful or interesting, a ⭐ would be appreciated!
