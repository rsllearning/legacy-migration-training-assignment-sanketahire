# AI-Assisted Legacy Migration Lab

## Scenario
You have been assigned a critical JIRA ticket: **Migrate the Social Media V1 API to a modern Spring Boot V2 Architecture.**

The V1 codebase (`com.social.legacy`) is undocumented, uses outdated raw `JdbcTemplate` queries, and is filled with hidden business rules. 

**Your Objective:** You must act as the lead engineer and actively use an AI Assistant (like ChatGPT, GitHub Copilot, or Claude) to map the legacy code, generate the modernized V2 codebase, and fix the hallucinations the AI inevitably introduces.

---

## Phase 1: AI Dependency Mapping
Legacy code is full of implicit rules. 
1. Provide your AI Assistant with the full context of the legacy files (Controllers and DAOs).
2. Prompt the AI: *"I need to migrate this legacy code. Before writing new code, analyze this and list all implicit business rules, hidden database queries, and security checks."*
3. Review the AI's output to ensure it understood the codebase.

## Phase 2: Establish the Golden Master
Before generating new code, you must lock down the current behavior. Start the application and capture the baseline V1 JSON payloads:

```bash
curl.exe -s -X GET "http://localhost:8080/api/v1/profile?user_id=1&requester_id=1" > golden_profile_1.json
curl.exe -s -X GET "http://localhost:8080/api/v1/profile?user_id=2&requester_id=3" > golden_profile_2.json
curl.exe -s -X POST "http://localhost:8080/api/v1/settings?user_id=1" -d "mute_notifications=true" > golden_settings.json
curl.exe -s -X GET "http://localhost:8080/api/v1/feed?user_id=1" > golden_feed.json
```

## Phase 3: AI Code Generation (The JIRA Constraints)
Now, prompt your AI to generate the `/modern` V2 codebase. You **must** include these exact 5 architectural guidelines in your prompt to steer the AI:

1. **Enforce Strict Separation of Concerns (N-Tier Architecture):** The codebase must be refactored into distinct `controller`, `service`, `repository`, `dto`, and `domain` layers. Controllers must act strictly as HTTP routers with zero embedded business logic.
2. **Modernize the Persistence Layer:** Fully deprecate raw `JdbcTemplate` queries and manual SQL mapping. Migrate all database interactions to Spring Data JDBC leveraging `@Table` entities and `CrudRepository` interfaces.
3. **Implement Strict Data Transfer Objects (DTOs):** Eliminate the use of loosely typed `Map<String, Object>` and raw `@RequestParam` parsing. All ingress and egress API payloads must be strongly typed via Java DTO classes.
4. **Centralize Global Exception Handling:** Remove manual `ResponseEntity` HTTP status returns from the business logic. The application must throw standard Java exceptions, which are intercepted and formatted globally by a `@ControllerAdvice` component.
5. **Maintain Absolute API Contract Integrity (Zero Regressions):** While the endpoints will be versioned to `/api/v2/...`, the structural shape, data types, and status codes of the JSON responses must perfectly match the V1 baseline to ensure backward compatibility for existing mobile clients.

*Place the code the AI generates into `src/main/java/com/social/modern/` and ensure the application compiles.*

## Phase 4: Red-Team & Prompt Refinement
AI models *always* drop context. Run your newly generated V2 endpoints and compare them against the `golden_*.json` files. 

When you find a bug (e.g., a dropped privacy check, a missing SQL filter, or a changed date format), **document the finding in your report**. Then, **do not manually fix the Java code**. Instead, write a *Correction Prompt* to the AI explaining its failure and asking it to output the fix.

---

## Deliverable: AI_MIGRATION_REPORT.md
Submit a report documenting your AI pairing session. It must include:
1. **The Prompts:** Provide the main prompts you used to generate the architecture.
2. **The Verification Tests:** Include the `curl` commands you ran to expose the bugs, noting exactly how the V1 and V2 JSON outputs differed.
3. **The Hallucinations:** List at least 3 bugs/regressions the AI introduced (e.g., missing data, security gaps).
4. **The Corrections:** Provide the follow-up correction prompts you used to force the AI to fix its own mistakes.
5. **Rollback Strategy:** First, think back to what you learned in training about API routing. Think about how V1 and V2 are currently running side-by-side on the exact same server. If V2 crashes in production, what is your exact plan to instantly roll back to V1 without taking the server offline or redeploying code?
