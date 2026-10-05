# ADR-001: Selection of Room ORM for Local Persistence

- **Status**: Proposed (Pending Human Approval)
- **Date**: 2026-10-05
- **Deciders**: Human Project Owner, Antigravity (Architect Role)
- **Technical Context**: SPEC-001 Gym Workout Logging

---

## Context & Problem Statement
The application requires local, offline-first storage for participants, exercises, workout sessions, and set logs. The project constitution ([AGENTS.md](file:///e:/AI%20Projects/GYM%20Tracker%20App/AGENTS.md)) specifies "Room or Sqlite for local offline-first storage". A decision is required on whether to use Android Jetpack Room ORM or raw SQLite (`SQLiteOpenHelper`).

## Decision Drivers
1. **Type Safety & Compile-Time Checks**: Prevent SQL syntax and schema mismatch bugs at compile time.
2. **StateFlow & Flow Integration**: First-class reactive observation of queries, aligning directly with Compose UI and MVVM StateFlow.
3. **Minimal Boilerplate**: Automatic mapping of cursor rows to Kotlin data classes.
4. **Constitution Compliance**: Strictly aligned with Android Jetpack standard library.

## Considered Options
1. **Jetpack Room ORM**
2. **Raw SQLite (`SQLiteOpenHelper` / `SupportSQLite`)**

## Decision Outcome
Chosen option: **Jetpack Room ORM**, because:
- It generates verified SQLite code with zero manual cursor parsing.
- Built-in coroutines `Flow` support streams live updates to ViewModels and Jetpack Compose screens seamlessly.
- Simplifies relations (`@Relation`, `@ForeignKey`) and transactional updates.

## Dependencies Proposed
- `androidx.room:room-runtime:2.6.1`
- `androidx.room:room-ktx:2.6.1`
- `androidx.room:room-compiler:2.6.1` (via KSP)

## Consequences
- **Positive**: High development speed, robust data integrity, less manual boilerplate, straightforward migrations.
- **Negative**: Adds KSP compiler plugin to Gradle build configuration.
