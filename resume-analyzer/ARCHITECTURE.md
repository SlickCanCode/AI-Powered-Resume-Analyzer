# ResuMatch Backend Architecture & Design

## 1. High-Level Architecture

ResuMatch is a **Spring Boot 3.4.4 REST API** application that parses resumes, analyzes them against job descriptions, and tracks user subscription quotas. The system follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Controllers                      │
│  (ResumeController, UserController, AuthController, etc.)   │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP Requests
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  (UserService, ResumeService, SubscriptionService, etc.)    │
│                  [Business Logic]                            │
└────────────────────┬────────────────────────────────────────┘
                     │ Repository Calls
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Data Access Layer                          │
│  (Spring Data JPA Repositories)                             │
└────────────────────┬────────────────────────────────────────┘
                     │ SQL Queries
                     ▼
┌─────────────────────────────────────────────────────────────┐
│           PostgreSQL / H2 Database                           │
│       (H2 in dev/test, PostgreSQL in prod)                  │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Framework**: Spring Boot 3.4.4 with Spring Data JPA
- **Security**: Spring Security 6.x with JWT tokens
- **Database**: PostgreSQL (prod), H2 (test)
- **Migrations**: Flyway (8 migrations covering all schema versions)
- **Resume Processing**: Apache Tika + Apache PDFBox + Tesseract OCR
- **LLM Integration**: Google Generative AI (Gemini)
- **File Storage**: Cloudinary 
- **Email Service**: Resend 
- **External APIs**: OkHttp3 for job posting fetching 
- **Serialization**: Jackson ObjectMapper for JSON

---

## 2. Core Domain Model

### Entity Relationships

```
User (root aggregate)
├── Resumes (OneToMany) ─┐
│                        └─→ UploadedResume 
│                             ├── ResumeData (OneToOne, structured fields) 
│                             └── ResumeAnalysis (OneToMany, score history)
├── Subscription (OneToOne)
│   └── SubscriptionUsage (OneToOne, billing period tracking)
└── VerificationToken (OneToMany, email verification)
```

### Key Entities

#### **User**
```java
@Entity @Table(name = "users")
public class User {
    UUID id;                           // Primary key
    String firstName, lastName, email; // Validation: @Email, username length, no special chars
    String password;                   // BCrypt encoded
    boolean emailVerified;             // Verification via token
    List<UploadedResume> resumes;      // User's documents
    Subscription subscription;         // Current plan
    List<VerificationToken> tokens;    // 1-time email verification tokens
}
```

#### **UploadedResume**
```java
@Entity @Table(name = "resumes")
public class UploadedResume {
    UUID id;                      // Primary key
    String filename, fileType;    // e.g., "resume.pdf", "application/pdf"
    @Lob String parsedContent;    // Full text extracted by Tika
    int latestScore;              // Most recent overall ATS score
    int analysisCount;            // Number of analyses performed
    LocalDateTime createdAt;      // Upload timestamp
    User user;                    // Owner reference (ManyToOne)
    ResumeData resumeData;        // Structured extraction result
    List<ResumeAnalysis> analysis; // Historical analyses
}
```

#### **ResumeData** (Structured Extraction)
```java
@Entity @Table(name = "resume_data")
public class ResumeData {
    UUID id;
    UploadedResume resume;        // Reference back to resume (OneToOne)
    String fullName, email, phone, location, careerSummary;
    List<String> skills;
    List<ResumeOnlineProfile> onlineProfiles;   // LinkedIn, GitHub, Portfolio URLs
    List<ResumeExperience> experience;          // Job history
    List<ResumeEducation> education;            // Degrees and certifications
}
```

#### **ResumeAnalysis** (Scoring & Feedback)
```java
@Entity @Table(name = "resume_analysis")
public class ResumeAnalysis {
    UUID id;
    UploadedResume resume;        // ManyToOne with foreign key index
    int overallScore;             // 0-100 aggregate score
    int atsScore;                 // ATS keyword match % (0-100)
    int keywordScore;             // Relevance to job description
    List<String> strengths, weaknesses, existingSkills, skillsToDevelop;
    List<AnalysisGrammerIssue> grammarIssues;      // Spelling/grammar feedback
    List<AnalysisRecommendation> recommendations;  // Actionable improvements
    LocalDateTime createdAt;      // Analysis timestamp (indexed for ordering)
}
```

#### **Subscription & SubscriptionUsage**
```java
@Entity @Table(name = "subscription")
public class Subscription {
    UUID id;
    User user;
    SubscriptionPlan plan;        // FREE, PRO, ENTERPRISE
    LocalDateTime currentPeriodStart, currentPeriodEnd;
    LocalDateTime createdAt;
}

@Entity @Table(name = "subscription_usage")
public class SubscriptionUsage {
    UUID id;
    User user;
    int analysesUsed;             // Count in current billing period
    LocalDateTime resetDate;      // When usage resets
}
```

### Database Indexes for Performance
```sql
-- ResumeAnalysis: Fast multi-user queries ordered by date
idx_resume_analysis_resume_id                -- Lookup by resume
idx_resume_analysis_created_at               -- Reverse chronological ordering
idx_resume_analysis_resume_created_at        -- Combined lookup + sort
```

---

## 3. Service Layer Design

All services follow these principles:
- **Transactional**: `@Transactional` at method level ensures atomicity
- **UUID Formatting**: Accepts both raw UUIDs and hyphenated strings; normalizes internally
- **Exception Handling**: Custom exceptions signal specific failure modes
- **Dependency Injection**: Spring `@Autowired` (field injection) for flexibility

### ResumeService

**Responsibilities**:
1. Parse resume files (PDF, DOCX, images) via Tika/OCR
2. Extract structured data via Gemini LLM
3. Analyze resumes against job descriptions
4. Track analysis history per resume

**Key Methods**:
```java
ResumeDataResponse parseFile(MultipartFile file, String jwt)
  → Validates file type, checks malformed PDFs, extracts text, calls Gemini

ResumeAnalysisResponse analyzeResume(String resumeId, String jobDescription)
  → Checks subscription quota, calls Gemini for scoring

JobMatchResponse analyzeJobMatch(String resumeId, String jobLink)
  → Fetches job posting, compares to resume via LLM

List<AnalysisSummaryResponse> getAllAnalyses(String jwt)
  → Projection query: only resumeName, score, atsScore, createdAt (optimized)

ResumeDataResponse getResumeData(String resumeId, String jwt)
  → Authorization check: resume must belong to authenticated user
```

### UserService

**Responsibilities**:
1. User registration with email uniqueness validation
2. Password encoding (BCrypt)
3. User profile updates
4. Account deletion and password resets

**Key Methods**:
```java
User saveUser(User user)
  → Encodes password, prevents duplicate verified emails
  → Auto-creates default FREE subscription

UserResponseDto updateUser(String jwt, UpdateuserRequest request)
  → Prevents email conflicts, mutates authenticated user only

void changePassword(String jwt, String currentPassword, String newPassword)
  → Validates old password before changing
```

### SubscriptionService

**Responsibilities**:
1. Create default FREE plan for new users
2. Track usage per billing period
3. Enforce quotas (rate limiting)
4. Handle plan upgrades/downgrades

**Key Methods**:
```java
void createDefaultSubscription(User user)
  → Called automatically on user registration

boolean hasAnalysisQuota(String userId)
  → Check remaining analyses before processing

void incrementAnalysisUsage(String userId)
  → Called after each analysis, throws RateLimitException if quota exceeded

void resetUsageForNewPeriod(String userId)
  → Billing period renewal logic
```

### AuthService & JwtService

**JWT Strategy**:
- **Access Token**: 15-minute lifetime, carries user ID
- **Refresh Token**: Longer-lived, used to get new access tokens
- **Storage**: HttpOnly cookies (immune to XSS)
- **Transport**: Cookie: `access_token` header in requests

**JWT Claims**:
```json
{
  "userId": "12345678-1234-1234-1234-1234567890ab",
  "exp": 1234567890,
  "iat": 1234567200
}
```

### GeminiService & JobPostingExtractor

**GeminiService** (LLM Integration):
```java
ResumeData parseResume(String resumeText)
  → Calls Google Gemini API with prompt to extract structured fields
  → Returns parsed: name, email, skills, experience, education, etc.

AnalysisResult analyzeResume(String resumeText, String jobDescription)
  → Scores resume: overallScore (0-100), atsScore, keywordScore
  → Generates strengths, weaknesses, recommendations
```

**JobPostingExtractor** (Web Scraping):
```java
String extract(String jobUrl)
  → Validates URL (http/https only, no localhost/file://)
  → Fetches HTML, prefers JSON-LD schema, falls back to visible text
  → Removes navigation/footer noise
```

---

## 4. API Design

### Request/Response Pattern

All endpoints follow a consistent contract:

```
POST   /api/v1/resumes/upload
       Requires: file (multipart), Cookie: access_token
       Returns: ResumeDataResponse

GET    /api/v1/resumes/analyses
       Requires: Cookie: access_token
       Returns: List<AnalysisSummaryResponse>

GET    /api/v1/resumes/:id
       Requires: access_token
       Returns: ResumeDataResponse

POST   /api/v1/resumes/:id/analyze
       Requires: access_token, body: { jobDescription }
       Returns: ResumeAnalysisResponse

POST   /api/v1/users
       No auth required
       Returns: RegisterResponse

GET    /api/v1/users/me
       Requires: access_token
       Returns: UserResponseDto

PUT    /api/v1/users/me
       Requires: access_token, body: { firstName, lastName, email }
       Returns: UserResponseDto
```

### Security Filter Chain

```
HTTP Request
  ↓
[ExceptionHandlerFilter] ← Catches auth exceptions, formats error JSON
  ↓
[AuthenticationFilter] ← Processes /api/v1/auth/login, sets session
  ↓
[JWTAuthorizationFilter] ← Extracts "access_token" cookie, validates JWT
  ↓
[Authorization Rules]
  ├─ /api/v1/auth/** → permitAll()
  ├─ POST /api/v1/users → permitAll()
  └─ /api/** → authenticated()
  ↓
[Controller + Service Layer]
  ↓
HTTP Response
```

### Response DTOs (Records, Immutable)

```java
record AnalysisSummaryResponse(
    UUID id,
    String resumeName,
    LocalDateTime dateTime,
    int score,
    int atsScore
) {}

@Builder
record ResumeDataResponse(
    String resumeId,
    String fullName, email, phone, location, summary,
    List<ResumeOnlineProfile> onlineProfiles,
    List<String> skills,
    List<ResumeExperience> experience,
    List<ResumeEducation> education
) {}
```

---

## 5. Design Principles Applied

### DRY (Don't Repeat Yourself)
- **Utility Methods**: `formatUUID()`, `unwrap()` centralized in services
- **Common Projections**: `findAllSummariesByUserId()` custom query prevents fetching entire analysis objects
- **Shared Exception Handling**: `@ControllerAdvice` formats all error responses consistently

### ETC (Easy to Change)
- **Interface-Based Services**: All services implement interfaces (`UserService`, `ResumeService`), allowing easy mocking/swapping
- **Dependency Injection**: Constructor injection via `@AllArgsConstructor` (Lombok) makes dependencies explicit
- **Configuration Externalized**: application.properties for DB URL, JWT secrets (not in code)

### SOLID Principles
- **Single Responsibility**: Each service owns one domain (User, Resume, Subscription)
- **Open/Closed**: Transactional boundaries are extensible without modifying existing code
- **Liskov Substitution**: Implementations respect interface contracts
- **Interface Segregation**: Services expose only necessary methods
- **Dependency Inversion**: High-level modules (controllers) depend on abstractions (interfaces), not concretions

### Error Handling Strategy
```
Custom Exceptions (Domain-Specific)
├── UserNotFound             → GET /api/v1/users/:id on invalid ID
├── EntityNotFoundException  → Resume/ResumeData access
├── DuplicateResourceException → Duplicate email registration
├── FileProcessingException  → Malformed PDF/DOCX
├── BadRequestException      → Invalid job posting URL
└── RateLimitException       → Subscription quota exceeded

→ All caught by @ControllerAdvice, serialized as JSON error response
```

---

## 6. Database Schema & Migrations

Flyway manages all schema changes in order:

```
V1__create_users_table.sql
V2__create_resumes_table.sql
V3__create_verification_token_table.sql
V4__create_resume_analysis_table.sql
V5__create_enums.sql
V6__create_subscription_table.sql
V7__create_subscription_usage_table.sql
V8__create_resume_data_table.sql
```

**Key Constraints**:
- Email uniqueness on User
- Foreign keys with CASCADE delete (resume → analysis)
- Indexes on frequent query predicates (resume_id, created_at)
- JSON columns for complex nested data (skills list, grammar issues)

---

## 7. Testing Strategy

### Unit Tests (Fast, Mocked)
- **Service Layer**: Mock repositories, test business logic isolation
- **Controller Layer**: Mock services, test request/response contracts
- Coverage: Happy path + error cases

### Integration Tests (Slower, Real DB)
- **Repository Layer**: H2 in-memory DB, Flyway migrations run
- **Service-to-DB Transactions**: Verify transactional behavior, query correctness
- Example: `ResumeAnalysisRepositoryIntegrationTest` checks multi-user isolation

### Contract Tests (MVC Slice)
- **@WebMvcTest**: Controller + security filters, no service/DB
- Validates endpoint paths, HTTP status codes, JSON response structure

**Test Pyramid**:
```
          / \  Integration Tests (Slow, High Confidence)
         /   \
        /     \ Unit Tests (Fast, Isolated)
       /       \
      /_________\ Contract Tests (Medium Speed)
```

---

## 8. Deployment Considerations

### Environment Configuration
```properties
# application.properties (checked in)
spring.jpa.hibernate.ddl-auto=validate  # Never auto-generate schema
spring.flyway.enabled=true               # Always run migrations

# application-dev.properties (for dev/test)
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# application-prod.properties (for production)
spring.datasource.url=jdbc:postgresql://host:5432/resumatch
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

### Scaling Considerations
- **Read-Heavy**: Use database replicas, cache frequently-queried analyses
- **Write-Heavy (Subscriptions)**: Consider event sourcing for audit trail
- **File Storage**: Cloudinary handles resume uploads (not local filesystem)
- **API Rate Limiting**: SubscriptionService enforces per-user quotas

---

## 9. Future Enhancements

1. **Caching**: Redis for frequently-accessed resume summaries
2. **Event-Driven**: Kafka events on analysis completion (async notifications)
3. **Metrics**: Prometheus for API latency, analysis volume tracking
4. **Search**: Elasticsearch for resume full-text search across users
5. **OAuth2 Providers**: Google/GitHub login (already scaffold in place)
6. **PDF Generation**: Export analyses as professional PDF reports
7. **Webhooks**: Notify external systems on analysis completion

---

## 10. Code Quality Standards

### Code Style
- **Naming**: Clear, intention-revealing names
- **Line Length**: Max 120 characters
- **Methods**: Single responsibility (ideally <20 lines)
- **Comments**: Document "why", not "what" (code is self-documenting)

### Testing Standards
- **Coverage Target**: 70%+ for service layer (business logic)
- **Test Naming**: Given-When-Then pattern visible in method names
- **Assertions**: One logical assertion per test (may have multiple lines)
- **Mocking**: Mock external dependencies (DB, LLM API, file storage)

### Security Standards
- **Password**: BCrypt with work factor 10+
- **JWT**: HS256 signature, 15-minute access token lifetime
- **Validation**: Input validation at controller layer (JSR-380 annotations)
- **SQL Injection**: Parameterized queries via Spring Data JPA
- **CORS**: Explicit allowed origins, credentials allowed only to same-site

---

## 11. Common Patterns in Use

### Null Safety
```java
Optional<Resume> resume = resumeRepository.findById(id);
Resume unwrapped = resume.orElseThrow(() -> 
    new EntityNotFoundException(id, Resume.class));
```

### Transactional Consistency
```java
@Transactional
public void analyzeResume(String id, String jobDescription) {
    UploadedResume resume = findById(id);  // Lazy-loaded
    ResumeAnalysis analysis = callGemini(resume.getParsedContent());
    resumeAnalysisRepository.save(analysis);
    subscriptionService.incrementAnalysisUsage(userId);
    // All committed together, or all rolled back on exception
}
```

### Authorization Pattern
```java
@GetMapping("/{id}")
public ResponseEntity<ResumeData> getResume(
    @PathVariable String id,
    @CookieValue(name = "access_token") String jwt
) {
    UUID userId = jwtService.extractUserId(jwt);  // From JWT
    ResumeData data = resumeDataRepository
        .findByResumeIdAndResumeUserId(id, userId)
        .orElseThrow(() -> new EntityNotFoundException(id, ResumeData.class));
    return ResponseEntity.ok(data);
}
```

---

## Summary

ResuMatch follows a clean, layered architecture with clear separation between the API contract, business logic, and data access. The codebase prioritizes:

✅ **Maintainability** through SOLID principles and DRY code  
✅ **Safety** via type-safe repositories, optional handling, transactions  
✅ **Testability** through dependency injection and mock-friendly interfaces  
✅ **Scalability** with index-aware queries and subscription-based rate limiting  
✅ **Security** with JWT + BCrypt + input validation  
