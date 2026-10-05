---
description: Planning-only workflow that reads a spec and produces an implementation plan with test mapping, entities, screens, and risk analysis.
---

# /architect

Takes a spec file path as an argument (e.g., `/architect docs/ai-sdlc/specs/SPEC-001.md`).

This is a **planning-only** workflow. Its purpose is to analyze an approved specification and produce a concrete, minimal implementation plan before any code or test is written.

## Strict Rules
- **Planning only**: Never edit or create source files (Kotlin, XML, Gradle, etc.) and never run commands.
- **Constitutional alignment**: Always read and strictly adhere to [AGENTS.md](file:///e:/AI%20Projects/GYM%20Tracker%20App/AGENTS.md).
- **Smallest change**: Prefer the smallest, cleanest change that fully satisfies the spec.
- **No silent decisions**: If a real architectural decision is needed, draft an ADR in the reply for human review instead of choosing silently.

## Instructions

1. **Read Context**:
   - Read `AGENTS.md` to confirm stack constraints, architecture rules, and dependencies.
   - Read the specification file provided in the argument (e.g., `docs/ai-sdlc/specs/SPEC-###.md`).

2. **Formulate the Plan**:
   Output a structured plan containing:
   - **Files to Create**: Comprehensive list of target file paths (Room database, entities, DAOs, repositories, ViewModels, Compose screens, theme, unit tests).
   - **Room Entities and Relations**: Schema definitions, tables, fields, types, primary keys, foreign keys, and relations.
   - **Screens and ViewModels**: UI screen hierarchy, Compose components, ViewModels, and `StateFlow` state contracts.
   - **Test List Mapped to Acceptance Criteria**: Explicit 1:1 mapping showing how each Acceptance Criterion (AC-1..AC-n) in the spec will be tested in JUnit.
   - **Risks**: Potential technical bottlenecks, state sync pitfalls, edge cases, and their mitigations.
   - **Rollback Note**: Clean rollback strategy to revert changes if needed.

3. **Architectural Decision Records (ADR)**:
   - If an architectural trade-off or undecided design choice arises, draft an ADR (title, status, context, decision, consequences) in the reply for human review and approval.
