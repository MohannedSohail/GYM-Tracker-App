# SPEC-001: Gym Workout Logging for Friends Group

- **Source Intent**: [intent/gym-workout-logging.md](file:///e:/AI%20Projects/GYM%20Tracker%20App/intent/gym-workout-logging.md)
- **Author**: Antigravity (Analyst Role)
- **Approved by**: Human User
- **Status**: approved

---

## 1. Problem & Context
Friends training together in the gym (typically 2–3 people) currently log their workouts in generic notes apps. This leads to disorganized logs, confusion over who lifted what weight, difficulty tracking set-by-set reps, and friction during the workout when users are fatigued and hands are sweaty. 

The goal of this feature is to provide a dedicated, offline-first Android application on a single shared device to smoothly manage workout sessions, select exercises, log weights (in KG) and reps per round per participant, and review past performance history.

---

## 2. Scope & Target Users
- **Target Users**: A group of 2–3 workout partners sharing one smartphone during gym sessions.
- **In Scope (v1)**:
  - Initial setup and persistent management of participant names.
  - Pre-populated library of common exercises + custom exercise creation.
  - Active workout session logging: recording round number, weight in KG, and reps for each person.
  - Ergonomic fast-input controls (`+` / `-` buttons) for weight and reps.
  - Set editing and deletion during the active session.
  - Soft-deletion / archiving of participants to maintain historical integrity.
  - Workout history view showing previous weights and reps per person (structured card/tabular view).
  - Full offline operation with Room/SQLite.
  - Arabic primary UI with RTL support and English fallback.
- **Out of Scope (Non-goals for v1)**:
  - Cloud synchronization, user authentication, or backend accounts.
  - Exercise demonstration videos / animations (scheduled for future releases).
  - Pre-designed workout programs / splits (scheduled for future releases).
  - Social media sharing or multi-device peer-to-peer sync.
  - Complex rest interval timers (deferred to keep v1 scope small and focused).

---

## 3. Conceptual Data Model
The local schema consists of:

1. **Participant** (`ParticipantEntity`):
   - `id`: Long (PK)
   - `name`: String
   - `isActive`: Boolean (defaults to `true`; set to `false` on soft delete)
   - `createdAt`: Long (epoch ms)

2. **Exercise** (`ExerciseEntity`):
   - `id`: Long (PK)
   - `nameAr`: String
   - `nameEn`: String
   - `category`: String (e.g., Chest, Back, Legs, Shoulders, Arms, Core)
   - `isCustom`: Boolean

3. **WorkoutSession** (`WorkoutSessionEntity`):
   - `id`: Long (PK)
   - `startTime`: Long (epoch ms)
   - `endTime`: Long? (epoch ms, nullable until session ends)
   - `notes`: String?

4. **SessionParticipant** (`SessionParticipantCrossRef`):
   - `sessionId`: Long (FK)
   - `participantId`: Long (FK)

5. **SetLog** (`SetLogEntity`):
   - `id`: Long (PK)
   - `sessionId`: Long (FK -> WorkoutSessionEntity.id)
   - `exerciseId`: Long (FK -> ExerciseEntity.id)
   - `participantId`: Long (FK -> ParticipantEntity.id)
   - `setNumber`: Int (1-indexed round)
   - `weightKg`: Double (e.g., 60.0, 62.5)
   - `reps`: Int (e.g., 10)
   - `loggedAt`: Long (epoch ms)

---

## 4. Functional Requirements

### FR-1: Participant Management
- **FR-1.1**: On initial app launch, if no active participants exist, prompt the user to input names of the friends group (minimum 1, recommended 2–3).
- **FR-1.2**: Participant names must persist locally across app restarts.
- **FR-1.3**: Users can add, edit, or soft-delete (archive) participants. Deleting a participant sets `isActive = false` and preserves all historical logs.
- **FR-1.4**: Duplicate active participant names or blank/whitespace names must be rejected.

### FR-2: Exercise Management
- **FR-2.1**: The app ships pre-populated with 10 core compound and isolation exercises:
  1. بنش برس مستوي (Flat Bench Press) - Chest
  2. بنش مائل دمبل/بار (Incline Press) - Chest
  3. سكوات (Squat) - Legs
  4. ديدليفت (Deadlift) - Back/Legs
  5. ضغط أكتاف بار/دمبل (Overhead Shoulder Press) - Shoulders
  6. سحب ظهر بار/كيبل (Barbell/Cable Row) - Back
  7. سحب دمبل فردي (One-Arm Dumbbell Row) - Back
  8. رفرفة أكتاف جانبي (Dumbbell Lateral Raise) - Shoulders
  9. تبادل بايسبس (Bicep Curl) - Arms
  10. دفع ترايسبس كيبل (Tricep Pushdown) - Arms
- **FR-2.2**: Users can create custom exercises by specifying an exercise name and muscle group category. Blank names are rejected.

### FR-3: Active Workout Session Flow
- **FR-3.1**: To start a workout, the user selects the active participants for today's session from the active participants list (`isActive == true`).
- **FR-3.2**: User selects an exercise to perform from the exercise library.
- **FR-3.3**: The session screen displays logging rows for each selected participant.
- **FR-3.4**: For each round/set, the user logs `weightKg` and `reps`.
- **FR-3.5**: Order of entry must be flexible (alternating round-by-round across participants or completing multiple rounds consecutively per person).
- **FR-3.6**: Users can edit the values of an already recorded set or remove a mistakenly added set during the session.
- **FR-3.7**: Ending the session timestamps the finish time and saves all sets permanently.

### FR-4: Fast In-Gym Logging UX
- **FR-4.1**: Weight input must include quick adjustment buttons: `+2.5`, `+5`, `-2.5`, `-5` kg, plus direct numeric input. Weights cannot be negative.
- **FR-4.2**: Reps input must include quick adjustment buttons: `+1`, `-1`, plus direct numeric input. Reps must be >= 1.
- **FR-4.3**: When adding a new round for a participant, pre-fill values with their previous round's weight and reps for that exercise.

### FR-5: Workout History & Progress
- **FR-5.1**: A dedicated History screen lists past completed sessions chronologically.
- **FR-5.2**: Inspecting a session reveals the exercises performed, participants present, and all sets (weights in KG and reps) recorded for each person in structured cards/tables.
- **FR-5.3**: While logging an active exercise, show the last recorded weight/reps for each participant from their previous session on that exercise as a reference target.

### FR-6: Localization & UI Design
- **FR-6.1**: Primary UI language is Arabic, with right-to-left (RTL) layout support. English fallback strings provided.
- **FR-6.2**: High-contrast typography and large tap targets suitable for gym environment.

---

## 5. Non-Functional Requirements (NFR)
- **NFR-1 (Offline-First)**: Zero network dependency; all operations execute against local Room database.
- **NFR-2 (Data Durability)**: Every set logged is committed to local storage immediately, preventing data loss if the app process is terminated.
- **NFR-3 (Process Death Recovery)**: If an active session is interrupted, reopening the app restores the active session state and all completed sets.
- **NFR-4 (Performance)**: UI interactions (incrementing/decrementing sets) must render under 16ms with zero noticeable frame drops.
- **NFR-5 (Architecture & Standards)**: Kotlin, Jetpack Compose (Material 3), MVVM (`ViewModel` + `StateFlow`), Room ORM, JUnit 4/5 unit tests.

---

## 6. Acceptance Criteria (Mapped 1:1 to Automated Tests)

### AC-1: Initial Participant Setup
- **Given** an empty database on first launch,
- **When** the user enters names "أحمد" and "محمد" and saves,
- **Then** both participants are stored in the database with `isActive = true` and appear in the participants list.

### AC-2: Participant Name Persistence
- **Given** participants "أحمد" and "محمد" already exist,
- **When** the application restarts,
- **Then** the saved participants are loaded immediately without re-prompting.

### AC-3: Custom Exercise Creation
- **Given** the pre-populated exercise catalog,
- **When** the user creates a new exercise "سحب ظهر واسع" under category "Back",
- **Then** the new exercise is saved with `isCustom = true` and appears in exercise selection.

### AC-4: Workout Session Initialization
- **Given** active participants A, B, and C,
- **When** the user starts a session selecting only A and B,
- **Then** a new `WorkoutSession` is created with current timestamp, associated only with A and B.

### AC-5: Set Logging with KG and Reps
- **Given** an active session with participants A and B on exercise "بنش برس",
- **When** set 1 is logged for participant A with 60.0 kg and 10 reps,
- **Then** a `SetLog` record is stored with `setNumber = 1`, `weightKg = 60.0`, `reps = 10`, linked to participant A, the exercise, and the active session.

### AC-6: Fast Step Controls
- **Given** a set row with weight 60.0 kg and 10 reps,
- **When** the user taps `+2.5` for weight and `+1` for reps,
- **Then** the displayed and stored values update to 62.5 kg and 11 reps.

### AC-7: Previous Set Pre-fill
- **Given** participant A has completed set 1 at 70.0 kg and 8 reps,
- **When** the user adds set 2 for participant A in the same exercise,
- **Then** the initial values for set 2 default to 70.0 kg and 8 reps.

### AC-8: Historical Performance Lookup
- **Given** a past session where participant A logged 80.0 kg x 6 reps on "سكوات",
- **When** the user starts a new session with "سكوات" for participant A,
- **Then** the UI displays the previous best/last performance (80.0 kg x 6 reps) as reference.

### AC-9: Session Completion
- **Given** an active workout session with multiple logged sets,
- **When** the user taps "Finish Workout",
- **Then** the session's `endTime` is updated and the session transitions to completed status.

### AC-10: Input Validation for Weight and Reps
- **Given** an active set entry row,
- **When** a user attempts to enter negative weight (`weightKg < 0`) or non-positive reps (`reps <= 0`),
- **Then** the system rejects the input or clamps it to the minimum allowed value (0 kg, 1 rep) and does not persist an invalid set.

### AC-11: Input Validation for Names
- **Given** the participant creation or exercise creation dialog,
- **When** the user attempts to save an empty string or whitespace-only name,
- **Then** the operation is rejected with a validation error and no database record is created.

### AC-12: Soft Deletion of Participant with Existing History
- **Given** participant A has 10 recorded sets in past sessions,
- **When** the user deletes participant A,
- **Then** participant A's record has `isActive` set to `false`, all historical `SetLog` entries remain intact, and participant A is excluded from the new session participant selector.

### AC-13: In-Session Set Modification and Deletion
- **Given** an active session with set 1 recorded for participant A (50.0 kg, 10 reps),
- **When** the user modifies set 1 to 55.0 kg or deletes set 1,
- **Then** the `SetLog` record is updated in the database accordingly or deleted, and round numbers re-index cleanly.

### AC-14: Active Session Recovery After App Restart
- **Given** an ongoing session with 3 logged sets that has not been explicitly finished,
- **When** the app process is terminated and reopened,
- **Then** the app identifies the unclosed session, restores its state, and displays the previously recorded sets.

---

## 7. Resolved Design Decisions
1. **Reporting & Comparisons View (v1)**: Confirmed as structured chronological cards/tables per participant per exercise. Graphical chart analytics deferred to v2.
2. **Pre-loaded Exercise Catalog**: Confirmed with 10 core compound and isolation exercises covering Chest, Back, Legs, Shoulders, and Arms.
3. **Rest Timer**: Excluded from v1 to keep scope minimal and focused; direct manual flow prioritized.

---
Status: approved
