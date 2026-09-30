# AI-Assisted Legacy Migration Report: Social Media API V1 to V2

**Author / Lead Engineer:** Sanket Ahire  
**Service:** Social Media API (`payroll-service` / `com.social`)  
**Target Architecture:** Modern Spring Boot 3.2.x, Spring Data JDBC, N-Tier Architecture, Global ControllerAdvice  
**Date:** September 30, 2026  

---

## Executive Summary

This report documents the end-to-end AI-assisted migration of the legacy Social Media V1 API (`com.social.legacy`) to a modernized Spring Boot V2 architecture (`com.social.modern`). 

Following the principles established in the training material (*"Migrating Legacy Modules Without Regression and Critically Reviewing AI-Generated Code"*), this migration was conducted using a strict 4-phase loop:
1. **AI Dependency Mapping & Legacy Archaeology:** Reverse-engineering undocumented business rules, hidden database queries, and security checks.
2. **Golden Master Safety Net:** Capturing baseline production payloads across all endpoints to establish an empirical ground truth.
3. **AI Code Generation with Strict JIRA Constraints:** Steering the AI assistant using 5 mandatory architectural guidelines (N-Tier separation, Spring Data JDBC, strong DTO typing, centralized exception handling, and 100% contract fidelity).
4. **Human-Led Red-Teaming & Prompt Refinement:** Auditing AI output, isolating semantic regressions, and driving AI prompt corrections until zero contract deviation was achieved.

---

## Phase 1: AI Dependency Mapping (Legacy Archaeology)

Legacy code tells us what it *currently does*, not what it was *intended to do*. To prevent the AI assistant from hallucinating or smoothing over critical edge cases, we executed a structured dependency mapping session across all legacy controllers, DAOs, schema, and seed data.

### 1.1 Context Provided to AI Assistant
The AI was supplied with the complete source code of:
* `com.social.legacy.controller.ProfileControllerV1`
* `com.social.legacy.controller.SettingsControllerV1`
* `com.social.legacy.controller.FeedControllerV1`
* `com.social.legacy.dao.LegacyProfileDao`
* `com.social.legacy.dao.LegacyFeedDao`
* Database Schema (`src/main/resources/schema.sql`) and Seed Data (`src/main/resources/data.sql`)

### 1.2 The Mapping Prompt
```text
I need to migrate this legacy code. Before writing new code, analyze this and list all implicit business rules, hidden database queries, and security checks.
```

---

### 1.3 Extracted Dependency Matrix & Implicit Rules

The mapping analysis surfaced several critical implicit contracts and anti-patterns that must be preserved during V2 code generation:

| Feature / Endpoint | Legacy Class & Lines | Hidden Business Rules & Implicit Contracts | Failure Risk if Overlooked by AI |
| :--- | :--- | :--- | :--- |
| **Profile Access**<br>`GET /api/v1/profile` | `LegacyProfileDao.java`<br>(Lines 23–40) | 1. **Privacy Gate:** If `is_private == true` AND `!userId.toString().equals(requesterId)`, return `HTTP 403 Forbidden` with body `{"error": "Profile is private"}`.<br>2. **Not Found Handling:** If `users` row does not exist, return `HTTP 404 Not Found` with body `{"error": "Not found"}`.<br>3. **Synthetic Field:** The response Map injects `"badges": []` (empty array) even though **no `badges` column exists in the database schema**. | **Security Bypass / Contract Break:** AI will likely drop the privacy check (exposing private profiles) or drop `"badges"` because it does not exist on the database entity. |
| **Settings Update**<br>`POST /api/v1/settings` | `SettingsControllerV1.java`<br>(Lines 17–27) | 1. **Parameter Validation:** Validates form/query param `mute_notifications`. Strictly accepts only `"true"` or `"false"`.<br>2. **Status 200 Payload:** Returns `HTTP 200 OK` with JSON `{"status": "success"}`.<br>3. **Status 400 Payload:** Any other value (or malformed input) returns `HTTP 400 Bad Request` with an **empty response body** (`ResponseEntity.badRequest().build()`). | **Payload Mismatch:** AI often returns a JSON error body on `400` instead of preserving the legacy empty body. |
| **Feed Generation**<br>`GET /api/v1/feed` | `LegacyFeedDao.java`<br>(Lines 19–28) | 1. **Soft Delete Filter:** Raw SQL contains `WHERE is_deleted = false`. Soft-deleted posts (e.g. post 103) must never be returned.<br>2. **Timestamp Ordering:** Must explicitly sort `ORDER BY created_at DESC`.<br>3. **Hardcoded Pagination:** Query enforces `LIMIT 2`, returning at most the top 2 newest active posts. | **Data Leakage & Regression:** Standard Spring Data JDBC `findByUserId` will leak deleted posts, ignore sorting, and return all rows without the `LIMIT 2` constraint. |

---

### 1.4 Red-Team Line Citation Audit (The 33% Sample Verification)

Applying the training material's verification technique, we audited the AI's claims directly against source code line numbers:

1. **Claim:** *Profile privacy is checked using string comparison against requester ID.*  
   **Code Verification (`LegacyProfileDao.java`, Line 27):**  
   `if (isPrivate && !userId.toString().equals(requesterId))` $\rightarrow$ **Verified True.**
2. **Claim:** *Feed query enforces hardcoded limit of 2 and excludes deleted posts.*  
   **Code Verification (`LegacyFeedDao.java`, Line 20):**  
   `SELECT id, content, created_at FROM posts WHERE user_id = ? AND is_deleted = false ORDER BY created_at DESC LIMIT 2` $\rightarrow$ **Verified True.**
3. **Claim:** *Profile response includes synthetic badges list.*  
   **Code Verification (`LegacyProfileDao.java`, Line 35):**  
   `response.put("badges", new ArrayList<>());` $\rightarrow$ **Verified True.**

---

## Phase 2: Establish the Safety Net (Golden Master Baseline)

Before generating any modernized V2 code, we established an empirical safety net by locking down the exact input/output behavior of the V1 API across 4 critical production scenarios.

### 2.1 Baseline Data Capture Execution
With the Spring Boot application running on port 8080 (`http://localhost:8080`), we executed the following 4 `curl` commands to generate the official Golden Master JSON files:

```bash
# Request 1: Fetch Alice's Profile (Public User, Self-Request)
curl -s -X GET "http://localhost:8080/api/v1/profile?user_id=1&requester_id=1" -o golden_profile_1.json

# Request 2: Fetch Bob's Private Profile (Private User, Unauthorized Requester Charlie)
curl -s -X GET "http://localhost:8080/api/v1/profile?user_id=2&requester_id=3" -o golden_profile_2.json

# Request 3: Update Settings (Valid Parameter)
curl -s -X POST "http://localhost:8080/api/v1/settings?user_id=1" -d "mute_notifications=true" -o golden_settings.json

# Request 4: Fetch Alice's Feed (Soft-deleted Post 103 filtered, Order Descending, Limit 2)
curl -s -X GET "http://localhost:8080/api/v1/feed?user_id=1" -o golden_feed.json
```

---

### 2.2 Golden Master Saved Payloads & Status Codes

Below is the verified record of the baseline responses:

#### Request 1: `golden_profile_1.json`
* **Endpoint:** `GET /api/v1/profile?user_id=1&requester_id=1`
* **HTTP Status Code:** `200 OK`
* **Captured Payload:**
```json
{"badges":[],"id":1,"follower_count":500,"username":"alice"}
```
* **Contractual Nuance:** The response contains `"badges": []`, `"id": 1`, `"follower_count": 500`, and `"username": "alice"`.

#### Request 2: `golden_profile_2.json`
* **Endpoint:** `GET /api/v1/profile?user_id=2&requester_id=3`
* **HTTP Status Code:** `403 Forbidden`
* **Captured Payload:**
```json
{"error":"Profile is private"}
```
* **Contractual Nuance:** Unauthorized access to a private profile must strictly yield HTTP 403 with the exact error message key `"error"`.

#### Request 3: `golden_settings.json`
* **Endpoint:** `POST /api/v1/settings?user_id=1` (`mute_notifications=true`)
* **HTTP Status Code:** `200 OK`
* **Captured Payload:**
```json
{"status":"success"}
```
* **Contractual Nuance:** Valid setting mutation returns a single key-value JSON object. (Note: invalid inputs return `400 Bad Request` with an empty body).

#### Request 4: `golden_feed.json`
* **Endpoint:** `GET /api/v1/feed?user_id=1`
* **HTTP Status Code:** `200 OK`
* **Captured Payload:**
```json
[{"created_at":1700000030,"content":"Wow, learning to code is fun","id":104},{"created_at":1700000010,"content":"Having a great day","id":102}]
```
* **Contractual Nuance:** Alice has 4 total posts in the database:
  - Post 104 (`created_at: 1700000030`, active) $\rightarrow$ **Included (1st)**
  - Post 103 (`created_at: 1700000020`, `is_deleted = true`) $\rightarrow$ **Filtered out**
  - Post 102 (`created_at: 1700000010`, active) $\rightarrow$ **Included (2nd)**
  - Post 101 (`created_at: 1700000000`, active) $\rightarrow$ **Excluded by `LIMIT 2`**

## Phase 3: AI Code Generation (The JIRA Constraints)

With the Golden Master baselines secured, we prompted the AI to generate the modernized V2 codebase in `src/main/java/com/social/modern/` strictly enforcing the 5 JIRA Architectural Guidelines.

### 3.1 The AI Code Generation Prompt
```text
Migrate the Social Media V1 API to a modern Spring Boot V2 Architecture in package com.social.modern.
You must adhere to these 5 architectural guidelines:

1. Enforce Strict Separation of Concerns (N-Tier Architecture): The codebase must be refactored into distinct controller, service, repository, dto, and domain layers. Controllers must act strictly as HTTP routers with zero embedded business logic.
2. Modernize the Persistence Layer: Fully deprecate raw JdbcTemplate queries and manual SQL mapping. Migrate all database interactions to Spring Data JDBC leveraging @Table entities and CrudRepository interfaces.
3. Implement Strict Data Transfer Objects (DTOs): Eliminate the use of loosely typed Map<String, Object> and raw @RequestParam parsing. All ingress and egress API payloads must be strongly typed via Java DTO classes.
4. Centralize Global Exception Handling: Remove manual ResponseEntity HTTP status returns from the business logic. The application must throw standard Java exceptions, which are intercepted and formatted globally by a @ControllerAdvice component.
5. Maintain Absolute API Contract Integrity (Zero Regressions): While the endpoints will be versioned to /api/v2/..., the structural shape, data types, and status codes of the JSON responses must perfectly match the V1 baseline to ensure backward compatibility for existing mobile clients.
```

---

### 3.2 Generated Architecture Layout

The AI generated an N-tier structure across 6 specialized packages under `com.social.modern`:

```
src/main/java/com/social/modern/
├── controller/
│   ├── ProfileControllerV2.java       # Pure router for /api/v2/profile
│   ├── SettingsControllerV2.java      # Pure router for /api/v2/settings
│   └── FeedControllerV2.java          # Pure router for /api/v2/feed
├── service/
│   ├── ProfileService.java            # Profile business orchestration
│   ├── SettingsService.java           # Settings validation & mutation
│   └── FeedService.java               # Feed retrieval orchestration
├── repository/
│   ├── UserRepository.java            # Spring Data JDBC CrudRepository for users
│   └── PostRepository.java            # Spring Data JDBC CrudRepository for posts
├── dto/
│   ├── ProfileResponseDto.java        # Strongly-typed user profile response
│   ├── SettingsRequestDto.java        # Strongly-typed settings request DTO
│   ├── SettingsResponseDto.java       # Strongly-typed settings response DTO
│   ├── FeedPostDto.java               # Strongly-typed feed item DTO
│   └── ErrorResponseDto.java          # Strongly-typed error envelope
├── domain/
│   ├── User.java                      # @Table("users") entity with @Id
│   └── Post.java                      # @Table("posts") entity with @Id
└── exception/
    ├── UserNotFoundException.java     # Thrown when user is absent
    ├── PrivateProfileException.java   # Thrown on unauthorized private profile access
    ├── InvalidSettingException.java   # Thrown on invalid setting parameter
    └── GlobalExceptionHandler.java    # Centralized @RestControllerAdvice
```

### 3.3 Initial Compilation Status
* **Command:** `./gradlew compileJava`
* **Result:** `BUILD SUCCESSFUL` (Zero compilation errors).
* **Observation:** While the AI-generated code conforms structurally to modern Spring Boot best practices and compiles cleanly, deeper semantic analysis and empirical testing against the Golden Master baseline are required in Phase 4 to identify dropped business rules and hallucinations.

---

*Note: Phase 4 (Red-Teaming & Refinements) and the Zero-Downtime Rollback Strategy will be documented in the subsequent sections.*
