## Subscription Feature - Testing & Verification Guide

### Quick Test Workflow

#### 1. **User Registration (Auto-Subscription)**
```bash
POST /api/v1/users
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

**Expected Result:**
- User created with UUID
- Subscription created (FREE plan)
- SubscriptionUsage created (0 used, 5 allowed)
- OTP sent to email

**Verify in Database:**
```sql
-- Check subscription was created
SELECT * FROM subscriptions WHERE user_id = '<user_uuid>';

-- Check usage was created
SELECT * FROM subscription_usage WHERE user_id = '<user_uuid>';
```

---

#### 2. **Check Subscription Status**
```bash
GET /api/v1/users/subscription
Cookie: access_token=<jwt_token>
```

**Expected Response:**
```json
{
  "plan": "FREE",
  "analysesAllowed": 5,
  "analysesUsed": 0,
  "analysesRemaining": 5,
  "usagePercentage": 0,
  "hasQuotaRemaining": true
}
```

---

#### 3. **Upload Resume**
```bash
POST /api/v1/resumes/upload
Cookie: access_token=<jwt_token>
Content-Type: multipart/form-data

file: <resume.pdf>
```

**Expected Result:**
- Resume uploaded and parsed
- ResumeData extracted

---

#### 4. **Analyze Resume (Quota Check)**
```bash
POST /api/v1/resumes/{resume_id}/analyze
Cookie: access_token=<jwt_token>
Content-Type: application/json

{
  "jobDescription": "Senior Java Developer with 5+ years Spring Boot experience..."
}
```

**Expected Flow:**
1. ✅ First analysis: Success (1/5 used)
2. ✅ Analyses 2-5: Success
3. ❌ 6th analysis: RateLimitException with message: "You have reached your monthly analysis limit. Please upgrade your subscription."

**Verify Usage Incremented:**
```bash
GET /api/v1/users/subscription
```

Response after 1 analysis:
```json
{
  "plan": "FREE",
  "analysesAllowed": 5,
  "analysesUsed": 1,
  "analysesRemaining": 4,
  "usagePercentage": 20,
  "hasQuotaRemaining": true
}
```

After 5 analyses:
```json
{
  "plan": "FREE",
  "analysesAllowed": 5,
  "analysesUsed": 5,
  "analysesRemaining": 0,
  "usagePercentage": 100,
  "hasQuotaRemaining": false
}
```

---

#### 5. **Quota Exceeded - Error Response**
```bash
POST /api/v1/resumes/{resume_id}/analyze
# After reaching 5 analyses with FREE plan
```

**Expected Response:**
```json
{
  "errors": [
    "You have reached your monthly analysis limit. Please upgrade your subscription."
  ]
}
```

**HTTP Status:** 400 (Bad Request) with RateLimitException

---

### Advanced Testing

#### Test Plan Upgrade
```bash
# Direct database update (for testing)
UPDATE subscriptions 
SET plan = 'PRO' 
WHERE user_id = '<user_uuid>';

UPDATE subscription_usage 
SET resumes_analyses_allowed = 100 
WHERE user_id = '<user_uuid>';
```

Then verify: `GET /api/v1/users/subscription`
- Should show: plan = "PRO", analysesAllowed = 100

---

#### Test Usage Reset
```bash
# Direct database reset (for testing)
UPDATE subscription_usage 
SET resume_analyses_used = 0 
WHERE user_id = '<user_uuid>';
```

---

### Edge Case Testing

#### 1. **Exact Quota Limit**
- User with 1 analysis remaining
- Submit 1 analysis → Success
- Submit next analysis → RateLimitException

#### 2. **Concurrent Requests**
- Send multiple analysis requests simultaneously
- Expected: First to execute increments usage, subsequent may fail if quota reached

#### 3. **UUID Format Variations**
- With dashes: `123e4567-e89b-12d3-a456-426614174000`
- Without dashes: `123e4567e89b12d3a456426614174000`
- Both should work due to formatUUID() helper

#### 4. **OAuth2 Registration**
- User registers via Google OAuth2
- Should auto-create subscription (if implemented in OAuth flow)

---

### Database Integrity Checks

```sql
-- Check referential integrity
SELECT u.id, s.plan, s.status, su.resume_analyses_used, su.resumes_analyses_allowed
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id
LEFT JOIN subscription_usage su ON u.id = su.user_id;

-- Check for orphaned records
SELECT * FROM subscriptions WHERE user_id NOT IN (SELECT id FROM users);

-- Check plan limits match config
SELECT DISTINCT plan FROM subscriptions;
-- Should only show: FREE, PRO

-- Check status values
SELECT DISTINCT status FROM subscriptions;
-- Should only show: ACTIVE, CANCELLED, EXPIRED (ACTIVE for new users)
```

---

### Load Testing Considerations

1. **Connection Pooling:** Ensure sufficient DB connections for concurrent analyses
2. **Transaction Isolation:** @Transactional ensures consistency under load
3. **UUID Index:** Queries on user_id are indexed for performance
4. **Lazy Loading:** Subscription loaded on demand, not eagerly

---

### Monitoring & Logging

Add these debug logs during testing:

1. **User Creation:**
   ```
   INFO UserServiceImpl: Creating default subscription for user {userId}
   ```

2. **Quota Check:**
   ```
   DEBUG SubscriptionServiceImpl: Checking quota for user {userId}: {used}/{allowed}
   ```

3. **Usage Increment:**
   ```
   INFO SubscriptionServiceImpl: Analysis usage incremented for user {userId}: {newCount}/{limit}
   ```

4. **Quota Exceeded:**
   ```
   WARN SubscriptionServiceImpl: User {userId} exceeded quota. Allowed: {limit}, Used: {used}
   ```

---

### Production Checklist

- [ ] Database migrations run successfully (V6, V7)
- [ ] Application starts without errors
- [ ] New users get FREE subscription by default
- [ ] Subscription status endpoint works
- [ ] Analysis quota enforcement active
- [ ] Error messages are user-friendly
- [ ] No SQL injection vulnerabilities
- [ ] Concurrent transactions handled correctly
- [ ] Logging captures usage patterns
- [ ] Performance acceptable under load

---

### Rollback Procedure (if needed)

```sql
-- Set all users back to unlimited (remove quota)
UPDATE subscription_usage 
SET resumes_analyses_allowed = 999999, resume_analyses_used = 0;

-- Disable quota checks in code temporarily
-- (Comment out hasAnalysisQuota() check in ResumeServiceImpl)
```
