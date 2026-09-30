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

*Note: Phase 2 (Golden Master Baselines), Phase 3 (AI Code Generation), Phase 4 (Red-Teaming & Refinements), and the Zero-Downtime Rollback Strategy will be documented in the subsequent sections as each milestone is completed.*
