# ResuMatch Backend - Documentation Index

## 📚 Complete Documentation Suite

Welcome! This directory contains comprehensive documentation of the ResuMatch backend codebase, architecture, and test suite. Start here to get oriented.

---

## 🚀 Quick Start

### New to ResuMatch?
1. Read: **[CODE_REVIEW_SUMMARY.md](CODE_REVIEW_SUMMARY.md)** (5 min read)
   - Overview of what we reviewed and fixed
   - 17/17 tests passing
   - Key architectural decisions

2. Read: **[ARCHITECTURE.md](ARCHITECTURE.md)** (20 min read)
   - Complete system design
   - Entity relationships
   - Service responsibilities
   - API design and security

3. Reference: **[TEST_SUITE_REPORT.md](TEST_SUITE_REPORT.md)** (10 min read)
   - What each test covers
   - How to run tests locally
   - Future testing roadmap

### Existing Developer?
1. Check: **[CODE_REVIEW_SUMMARY.md](CODE_REVIEW_SUMMARY.md)** → Highlights section
   - Key architectural patterns to follow
   - Design principles in place

2. Run: Tests to see current state
   ```bash
   mvn clean test
   # Expected: Tests run: 17, Failures: 0, Errors: 0
   ```

3. Reference: **[ARCHITECTURE.md](ARCHITECTURE.md)** when implementing features
   - Copy existing patterns (DRY principle)
   - Follow authorization pattern (user isolation checks)
   - Match service layer contracts

---

## 📖 Documentation Files

### 1. **CODE_REVIEW_SUMMARY.md** ⭐ START HERE
**Purpose**: Executive summary of the entire codebase review

**Contents**:
- Overview: 17/17 tests passing, 100% success rate
- Architecture assessment with key patterns
- All issues found and fixed with explanations
- Test coverage highlights showing what's being verified
- Design principles verified (DRY, ETC, SOLID)
- Code quality metrics
- Recommendations for improvement

**Best for**: Getting oriented, understanding what was reviewed and fixed

**Time to read**: 10-15 minutes

---

### 2. **ARCHITECTURE.md** ⭐ REFERENCE
**Purpose**: Comprehensive system design documentation

**Contents**:
1. High-level layered architecture
2. Complete domain model with entity relationships
3. Database schema and Flyway migrations
4. Service layer responsibilities and contracts
5. API design and HTTP request patterns
6. Security filter chain and JWT handling
7. Testing strategy and test pyramid
8. Deployment considerations
9. Design principles in place (DRY, ETC, SOLID)
10. Common patterns used throughout codebase
11. Future enhancements and scaling

**Best for**: Understanding system design, implementing features, onboarding

**Time to read**: 20-30 minutes

**Sections to reference often**:
- Section 3: Service Layer Design (when adding features)
- Section 4: API Design (when adding endpoints)
- Section 10: Common Patterns (when writing code)

---

### 3. **TEST_SUITE_REPORT.md** ⭐ FOR TESTING
**Purpose**: Detailed test suite analysis and testing guide

**Contents**:
- Test results: 17/17 passing
- Test breakdown by file and category
- All changes made to fix tests
- Design principles applied in test updates
- Test coverage analysis by architectural layer
- Testing best practices implemented
- Future test coverage gaps and priorities
- Build and execution commands

**Best for**: Running tests, understanding what's tested, adding new tests

**Time to read**: 15-20 minutes

**Sections to reference often**:
- "Test Coverage Analysis" (what's tested by layer)
- "Future Test Coverage Gaps" (what to test next)
- "Build & Execution" (how to run tests)
- "Testing Best Practices" (patterns to follow for new tests)

---

## 🎯 How to Use This Documentation

### Scenario: I want to add a new feature

1. **Understand the architecture**:
   - Read ARCHITECTURE.md Section 3 (Service Layer Design)
   - Find similar existing feature
   - Copy the pattern

2. **Follow the authorization pattern**:
   - ARCHITECTURE.md Section 10 shows the pattern
   - Extract user from JWT
   - Load entity from repository
   - Verify ownership
   - Throw EntityNotFoundException if not authorized

3. **Write tests following existing patterns**:
   - TEST_SUITE_REPORT.md Section "Testing Best Practices"
   - Use same naming convention: `methodUnderTest_Scenario_ExpectedBehavior()`
   - Mock external dependencies in unit tests
   - Use real DB in integration tests

### Scenario: I found a bug

1. **Identify the layer**:
   - Web/Controller layer? → Check ResumeControllerTest pattern
   - Service layer? → Check UserServiceImplTest or ResumeServiceImplTest pattern
   - Repository layer? → Check ResumeAnalysisRepositoryIntegrationTest pattern

2. **Write a test that fails**:
   - TEST_SUITE_REPORT.md → Testing Best Practices
   - Example: UserServiceImplTest#getUserThrowsNotFoundForUnknownId

3. **Reference existing patterns**:
   - ARCHITECTURE.md Section 10 → Common Patterns
   - Understand current implementation
   - Fix the bug
   - Verify test passes

### Scenario: I need to optimize a query

1. **Understand query patterns**:
   - ARCHITECTURE.md Section 3 → "ResumeService"
   - Look for custom @Query methods
   - Check TEST_SUITE_REPORT.md for verification tests

2. **Test the optimization**:
   - Add integration test like ResumeAnalysisRepositoryIntegrationTest
   - Verify query returns only needed fields
   - Confirm performance improvement

---

## 🏗️ Codebase Structure

```
resume-analyzer/
├── 📄 ARCHITECTURE.md                  ← System design (READ THIS)
├── 📄 CODE_REVIEW_SUMMARY.md          ← Review summary (START HERE)
├── 📄 TEST_SUITE_REPORT.md            ← Testing guide (FOR TESTING)
├── 📄 README.md                        ← (if exists)
├── pom.xml                             ← Maven dependencies
│
├── src/main/java/com/slickdev/resume_analyzer/
│   ├── web/                           ← REST Controllers
│   │   ├── ResumeController.java
│   │   ├── UserController.java
│   │   ├── AuthController.java
│   │   └── DashboardController.java
│   │
│   ├── service/                       ← Business Logic (Interfaces)
│   │   ├── ResumeService.java
│   │   ├── UserService.java
│   │   └── impl/                      ← Service Implementations
│   │       ├── ResumeServiceImpl.java
│   │       ├── UserServiceImpl.java
│   │       └── ... (11 services total)
│   │
│   ├── repositories/                 ← Data Access (Spring Data JPA)
│   │   ├── ResumeRepository.java
│   │   ├── UserRepository.java
│   │   ├── ResumeAnalysisRepository.java
│   │   └── ... (7 repositories total)
│   │
│   ├── entities/                     ← Domain Models
│   │   ├── User.java
│   │   ├── UploadedResume.java
│   │   ├── ResumeData.java
│   │   ├── ResumeAnalysis.java
│   │   ├── Subscription.java
│   │   └── VerificationToken.java
│   │
│   ├── security/                     ← JWT + Spring Security
│   │   ├── SecurityConfig.java
│   │   ├── filters/
│   │   │   ├── JWTAuthorizationFilter.java
│   │   │   ├── AuthenticationFilter.java
│   │   │   └── ExceptionHandlerFilter.java
│   │   └── manager/
│   │       └── CustomAuthenticationManager.java
│   │
│   ├── exception/                    ← Custom Exceptions
│   │   ├── EntityNotFoundException.java
│   │   ├── UserNotFound.java
│   │   ├── DuplicateResourceException.java
│   │   └── ... (more exception types)
│   │
│   ├── requests/                     ← Request DTOs
│   │   ├── RegisterRequest.java
│   │   ├── UpdateuserRequest.java
│   │   └── ... (request types)
│   │
│   ├── responses/                    ← Response DTOs (Records)
│   │   ├── AnalysisSummaryResponse.java
│   │   ├── ResumeDataResponse.java
│   │   ├── UserResponseDto.java
│   │   └── ... (response types)
│   │
│   └── ResumeAnalyzerApplication.java ← Spring Boot Entry Point
│
├── src/main/resources/
│   ├── application.properties         ← Default config
│   ├── application-dev.properties     ← Dev config (H2)
│   ├── application-prod.properties    ← Prod config (PostgreSQL)
│   └── db/migration/                  ← Flyway Schema Versions
│       ├── V1__create_users_table.sql
│       ├── V2__create_resumes_table.sql
│       └── ... (8 migrations total)
│
└── src/test/java/com/slickdev/resume_analyzer/
    ├── service/
    │   ├── UserServiceImplTest.java      ✅ 6 tests
    │   ├── ResumeServiceImplTest.java    ✅ 5 tests
    │   └── JobPostingExtractorTest.java  ✅ 3 tests
    ├── repository/
    │   └── ResumeAnalysisRepositoryIntegrationTest.java  ✅ 1 test
    └── web/
        └── ResumeControllerTest.java     ✅ 2 tests
```

---

## 🔍 Key Concepts to Understand

### 1. **Layered Architecture**
```
Controllers (Web Layer)
    ↓ depends on
Services (Business Logic)
    ↓ depends on
Repositories (Data Access)
    ↓ depends on
Database (Persistence)

Rule: Never skip a layer (e.g., controller talking directly to DB)
```
See: ARCHITECTURE.md Section 1

### 2. **Authorization Pattern**
```
Every sensitive endpoint:
  1. Extract user ID from JWT
  2. Load data from repository
  3. Verify ownership (userId must match)
  4. Throw EntityNotFoundException if not authorized
  
Result: User A cannot access User B's data
```
See: ARCHITECTURE.md Section 10

### 3. **Service Transactionality**
```
All service methods marked @Transactional:
  - Multiple DB operations treated as atomic unit
  - All succeed or all rollback
  - No half-created state possible
```
See: ARCHITECTURE.md Section 11

### 4. **Exception Hierarchy**
```
Domain-Specific Exceptions:
  - UserNotFound(id)                    → User lookup failed
  - EntityNotFoundException(id, type)   → Generic entity lookup failed
  - DuplicateResourceException(field)   → Unique constraint violated
  - FileProcessingException(message)    → Resume parsing failed
  - BadRequestException(message)        → Invalid input (e.g., URL)
  - RateLimitException(message)         → Subscription quota exceeded
```
See: CODE_REVIEW_SUMMARY.md → Exception Hierarchy

### 5. **Query Optimization Pattern**
```
Do NOT:  resumeAnalysisRepository.findAll().filter(...)
Do:      resumeAnalysisRepository.findAllSummariesByUserId(userId)
         └─ Custom @Query returning AnalysisSummaryResponse projection
         └─ Fetches only: id, resumeName, dateTime, score, atsScore
         
Result: Smaller memory footprint, faster transfer
```
See: TEST_SUITE_REPORT.md → "Query Optimization Testing"

### 6. **JWT + Cookie Pattern**
```
Registration/Login:
  1. Verify credentials
  2. Generate access token (15 min) + refresh token (longer)
  3. Set in HttpOnly cookies
  4. Return success response

Request (User provides credentials in cookie):
  1. JWTAuthorizationFilter extracts "access_token" cookie
  2. Validates JWT signature
  3. Extracts userId from claims
  4. Sets SecurityContext
  
Result: XSS-proof (HttpOnly), CSRF protection available
```
See: ARCHITECTURE.md Section 4

---

## 📊 Test Statistics

```
┌──────────────────────────────────────────────────────┐
│ Test Suite Summary                                   │
├──────────────────────────────────────────────────────┤
│ Total Tests:           17                            │
│ Passing:              17  ✅ 100%                    │
│ Failing:               0                             │
│ Skipped:               0                             │
│                                                      │
│ Execution Time:       20.8 seconds                   │
│ Unit Tests:            14 (~2 sec)  FAST            │
│ Integration Tests:      1 (~14 sec) SLOW            │
│ MVC Tests:              2 (~2 sec)  MEDIUM          │
│                                                      │
│ Coverage Target:       70%+ for service layer       │
│ Current Coverage:      ~60% (improving)             │
└──────────────────────────────────────────────────────┘
```

---

## 🚀 Development Workflow

### Daily Developer Tasks

1. **Morning**: Pull latest code
   ```bash
   git pull origin main
   mvn clean test  # Verify nothing broke overnight
   ```

2. **Implement Feature/Fix Bug**
   - Write test first (TDD)
   - Implement feature/fix
   - Verify test passes
   - Reference ARCHITECTURE.md for patterns

3. **Before Committing**
   ```bash
   mvn clean verify              # Full pipeline
   mvn spotbugs:check           # Static analysis (if configured)
   mvn pmd:check               # Code style (if configured)
   ```

4. **Create Pull Request**
   - Tests must pass
   - Reference relevant documentation
   - Explain deviation from patterns (if any)

---

## ❓ FAQ

**Q: Where do I find examples of authorization checks?**  
A: See CODE_REVIEW_SUMMARY.md → "Authorization Testing" section. Examples: `getResumeDataDoesNotExposeAnotherUsersResume`, `updateUserRejectsAnotherUsersEmail`

**Q: How do I add a new endpoint?**  
A: Follow ResumeController pattern. (1) Create controller method, (2) Create service method with authorization, (3) Write test.

**Q: What exception should I throw?**  
A: See CODE_REVIEW_SUMMARY.md → "Exception Hierarchy" or ARCHITECTURE.md → Section 4

**Q: How do I run one specific test?**  
A: See TEST_SUITE_REPORT.md → "Build & Execution" section

**Q: What's the current test coverage?**  
A: ~60% overall, 75%+ in service layer. See TEST_SUITE_REPORT.md → "Future Test Coverage Gaps" for what to test next.

**Q: How do I mock a service in a test?**  
A: See TEST_SUITE_REPORT.md → "Mocking Strategy" section. Pattern shown in UserServiceImplTest.

---

## 📞 Getting Help

1. **Questions about architecture?**  
   → Read ARCHITECTURE.md (sections 1-3)

2. **Need to add tests?**  
   → Read TEST_SUITE_REPORT.md (sections on patterns and best practices)

3. **Bug in specific layer?**  
   → Find similar component in codebase using patterns from ARCHITECTURE.md

4. **Want to understand how something was fixed?**  
   → See CODE_REVIEW_SUMMARY.md → "What Was Fixed" (Issues #1-4)

---

## ✨ Last Updated

- **Date**: 2026-09-01
- **Tests**: 17/17 passing ✅
- **Build Time**: 20.8 seconds
- **Documentation**: Complete

---

**Happy coding! 🚀**

For questions or improvements to documentation, please refer to the team lead or create an issue in the repository.
