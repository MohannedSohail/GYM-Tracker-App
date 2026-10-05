# Project Constitution

## 1. What the Project Is
An Android app to log exercises, rounds (sets), and reps per round, per named person in a group of friends.

## 2. Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with ViewModel and StateFlow
- **Storage**: Room or Sqlite for local offline-first storage
- **Testing**: JUnit for tests
- **Backend**: No backend in v1

## 3. Workflow
```
intent.md -> SPEC-###.md -> plan.md -> tests -> code -> review
```
- Never write code before an approved spec and plan.
- Never mark an intent, spec, or plan as approved; only the human does.
- Open questions are answered or deferred by the human, never decided silently.

## 4. Where Things Live
- `intent/`: Problem descriptions and initial intents (`intent.md`)
- `docs/ai-sdlc/specs/`: Approved feature specifications (`SPEC-###.md`)
- `docs/ai-sdlc/adr/`: Architecture Decision Records

## 5. Hard Rules
- **No new dependency without asking**: Always ask before adding any dependency.
- **Small changes per task**: Keep scope small and focused.
- **No secrets or personal data**: Never include secrets or personal data in code or logs.
- **Acceptance criteria mapping**: Every acceptance criterion maps to a test.
