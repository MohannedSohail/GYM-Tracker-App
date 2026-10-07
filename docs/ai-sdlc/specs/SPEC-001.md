# SPEC-001: Gym Workout Logging for Friends Group

- **Source Intent**: [intent/gym-workout-logging.md](file:///e:/AI%20Projects/GYM%20Tracker%20App/intent/gym-workout-logging.md)
- **Author**: Antigravity (Analyst Role)
- **Status**: draft

---

## 1. Problem & Context
Friends training together in the gym (typically 2–3 people) currently log their workouts in generic notes apps. This causes confusion, disorganized logs, lost historical weights, and friction during workouts when users are fatigued and hands are sweaty.

This feature provides a dedicated, offline-first Android application on a single shared device to manage workout sessions, select exercises with measurable weights, record rounds (weight in KG + reps) per participant with ergonomic fast-adjustment controls, and review past session history organized by session.

---

## 2. Scope & Target Users
- **Target Users**: A group of 2–3 workout partners sharing one smartphone during gym sessions.
- **In Scope (v1)**:
  - Initial setup and persistent management of participant names.
  - Concept of **Session**: training session with date, start time, and end time.
  - Definition of **Round**: weight in KG (decimals supported, e.g. 22.5) + reps for one person in one exercise.
  - Pre-populated library of 30 common loadable exercises + custom exercise creation.
  - Active workout session logging: recording round number, weight in KG, and reps for each person.
  - Ergonomic fast-input controls (`+` / `-` 2.5 kg for weight, `+` / `-` 1 for reps, direct numeric tap-and-type).
  - Pre-filling input from the person's last recorded value for that exercise, with full ability to decrease or increase it.
  - In-session editing and deletion of recorded rounds.
  - Soft-deletion / archiving of friends with existing history (hides from active list while preserving all past records in history).
  - Workout history and reports: simple tabular/card list of dates and numbers per person organized by session.
  - Local offline storage only (Room SQLite); data lives strictly on device.
  - Arabic-only UI with full Right-to-Left (RTL) layout.
- **Out of Scope (Non-goals for v1)**:
  - Charts and graphical analytics (deferred to future releases).
  - English language support (deferred; Arabic only in v1).
  - Exercises without weight or measured by time (weight is strictly required for every round).
  - Cloud synchronization, backend, or user authentication.
  - Backup and data export (deferred).
  - Exercise demonstration videos / animations.
  - Pre-designed workout programs / splits.
  - Social media sharing.

---

## 3. Conceptual Data Model
The local schema consists of:

1. **Participant** (`ParticipantEntity`):
   - `id`: Long (PK autoincrement)
   - `name`: String
   - `isActive`: Boolean (defaults to `true`; set to `false` on soft delete)
   - `createdAt`: Long (epoch ms)

2. **Exercise** (`ExerciseEntity`):
   - `id`: Long (PK autoincrement)
   - `name`: String (Arabic exercise name)
   - `category`: String (الصدر، الظهر، الأرجل، الأكتاف، البايسبس، الترايسبس، السواعد، الترابيس، البطن)
   - `isCustom`: Boolean

3. **WorkoutSession** (`WorkoutSessionEntity`):
   - `id`: Long (PK autoincrement)
   - `startTime`: Long (epoch ms)
   - `endTime`: Long? (epoch ms, nullable until session ends)
   - `notes`: String?

4. **SessionParticipant** (`SessionParticipantCrossRef`):
   - `sessionId`: Long (FK -> WorkoutSessionEntity.id)
   - `participantId`: Long (FK -> ParticipantEntity.id)

5. **SetLog** (`SetLogEntity`):
   - `id`: Long (PK autoincrement)
   - `sessionId`: Long (FK -> WorkoutSessionEntity.id)
   - `exerciseId`: Long (FK -> ExerciseEntity.id)
   - `participantId`: Long (FK -> ParticipantEntity.id)
   - `setNumber`: Int (1-indexed round)
   - `weightKg`: Double (e.g., 22.5, 60.0; supports decimals)
   - `reps`: Int (integer >= 1)
   - `loggedAt`: Long (epoch ms)

---

## 4. Proposed Default Exercises (30 Exercises)
> **Proposed, pending human approval**  
> *Traceability*: Traced directly to the author delegation line in [intent/gym-workout-logging.md](file:///e:/AI%20Projects/GYM%20Tracker%20App/intent/gym-workout-logging.md) under `Desired outcome`:
> *"تفويض المحلل (Analyst) بشكل صريح لاقتراح قائمة تضم 30 تمريناً في المواصفات (SPEC) تغطي المجموعات العضلية التالية: الصدر، الظهر، الأكتاف، الأرجل، البايسبس، الترايسبس، السواعد، الترابيس، والبطن..."*  
> All 30 proposed exercises are balanced (3 to 4 exercises per muscle group) and require a measurable load (barbell, dumbbell, cable, machine). This list is proposed and not final until human review and approval.

### 1. الصدر (Chest) - 4 تمارين
1. **بنش برس بالبار مستوي** (Flat Barbell Bench Press) - بار
2. **تجميع بالدمبل مائل** (Incline Dumbbell Press) - دمبل
3. **تفتيح بالكيبل للصدر** (Cable Chest Flyes) - كيبل
4. **جهاز ضغط الصدر** (Chest Press Machine) - جهاز

### 2. الظهر (Back) - 4 تمارين
5. **سحب ظهر بالبار منحني** (Barbell Bent-Over Row) - بار
6. **سحب ظهر عمودي كيبل** (Lat Pulldown) - كيبل
7. **سحب دمبل فردي** (One-Arm Dumbbell Row) - دمبل
8. **سحب أرضي بالكيبل جالس** (Seated Cable Row) - كيبل

### 3. الأرجل (Legs) - 4 تمارين
9. **سكوات بالبار** (Barbell Back Squat) - بار
10. **جهاز دفع الأرجل** (Leg Press Machine) - جهاز
11. **ديدليفت روماني بالبار/الدمبل** (Romanian Deadlift) - بار/دمبل
12. **جهاز مد أو ثني الأرجل** (Leg Extension / Leg Curl Machine) - جهاز

### 4. الأكتاف (Shoulders) - 3 تمارين
13. **ضغط أكتاف بالبار واقف** (Overhead Barbell Shoulder Press) - بار
14. **ضغط أكتاف بالدمبل جالس** (Seated Dumbbell Shoulder Press) - دمبل
15. **رفرفة جانبي بالدمبل** (Dumbbell Lateral Raise) - دمبل

### 5. البايسبس (Biceps) - 3 تمارين
16. **مرجحة بايسبس بالبار** (Barbell Bicep Curl) - بار
17. **مرجحة بايسبس تبادل بالدمبل** (Dumbbell Alternating Bicep Curl) - دمبل
18. **مرجحة هامر بالكيبل بالحبل** (Cable Rope Hammer Curl) - كيبل

### 6. الترايسبس (Triceps) - 3 تمارين
19. **دفع ترايسبس بالكيبل بالحبل** (Cable Rope Tricep Pushdown) - كيبل
20. **كسر الجمجمة بالبار EZ** (EZ-Bar Skull Crusher) - بار
21. **ضغط ترايسبس بالدمبل فوق الرأس** (Overhead Dumbbell Triceps Extension) - دمبل

### 7. السواعد (Forearms) - 3 تمارين
22. **ثني المعصم بالبار جالس** (Seated Barbell Wrist Curl) - بار
23. **ثني المعصم العكسي بالبار أو الكيبل** (Reverse Barbell/Cable Wrist Curl) - بار/كيبل
24. **مرجحة معصم بالدمبل فردي** (One-Arm Dumbbell Wrist Curl) - دمبل

### 8. الترابيس (Traps) - 3 تمارين
25. **هزات أكتاف بالبار** (Barbell Shrugs) - بار
26. **هزات أكتاف بالدمبل** (Dumbbell Shrugs) - دمبل
27. **هزات أكتاف بالكيبل** (Cable Shrugs) - كيبل

### 9. البطن (Abs) - 3 تمارين
28. **طحن البطن بالكيبل جالس** (Cable Kneeling Crunch) - كيبل
29. **جهاز طحن البطن بالأوزان** (Machine Loaded Crunch) - جهاز
30. **دوران الجذع بالكيبل** (Cable Woodchopper / Loaded Rotation) - كيبل

---

## 5. Functional Requirements

### FR-1: Participant Management
- **FR-1.1**: On initial app launch, prompt to enter participant names (minimum 1, recommended 2–3).
- **FR-1.2**: Participant names persist locally across app restarts.
- **FR-1.3**: Friends can be edited or deleted. If a friend has logged rounds, deletion must perform a **Soft Delete** (`isActive = false`), hiding them from the active selector while retaining all historical session logs.
- **FR-1.4**: Blank, whitespace-only, or duplicate active participant names are rejected.

### FR-2: Exercise Management
- **FR-2.1**: App ships pre-populated with the 30 default loadable exercises (Section 4) once approved.
- **FR-2.2**: Users can create custom exercises specifying an Arabic name and a muscle category. All exercises must have loadable weights; bodyweight-only or time-based exercises are disallowed.

### FR-3: Session & Round Logging Flow
- **FR-3.1**: A **Session** represents a workout session having a start timestamp, end timestamp, and a set of participating friends.
- **FR-3.2**: A **Round** consists of: `weightKg` (decimal supported) + `reps` (integer >= 1) for one person in one exercise. Weight is mandatory.
- **FR-3.3**: Flexible turn order: participants can alternate sets round-by-round, or one participant can log multiple sets consecutively.
- **FR-3.4**: Users can edit recorded rounds or delete mistakenly added rounds in the active session.
- **FR-3.5**: Finishing the workout session records `endTime` and closes the session.

### FR-4: Fast In-Gym Logging UX
- **FR-4.1**: Input for each participant and exercise starts pre-filled from their **last recorded value** for that exercise, with full freedom to increase or decrease the value.
- **FR-4.2**: Weight step controls: `+2.5` and `-2.5` kg buttons. Tapping the numeric value allows direct keyboard entry of exact decimals (e.g. 22.5).
- **FR-4.3**: Reps step controls: `+1` and `-1` buttons, with direct numeric keyboard entry.

### FR-5: Workout History & Reports
- **FR-5.1**: History is structured and organized strictly by **Session**.
- **FR-5.2**: Displayed as a clean tabular/card list of dates, participants, exercises, weights (KG), and reps.

### FR-6: Localization & Platform
- **FR-6.1**: Arabic-only user interface with full Right-to-Left (RTL) layout.
- **FR-6.2**: Data lives exclusively on the local device using Room SQLite.

---

## 6. Acceptance Criteria (Mapped 1:1 to Automated Tests)

### AC-1: Initial Participant Setup
- **Given** an empty database on first launch,
- **When** the user enters names "أحمد" and "محمد" and saves,
- **Then** both participants are stored in the database with `isActive = true` and appear in the active participants list.

### AC-2: Participant Name Persistence
- **Given** participants "أحمد" and "محمد" already exist,
- **When** the application restarts,
- **Then** the saved participants are loaded immediately without re-prompting.

### AC-3: Custom Exercise Creation
- **Given** the pre-populated exercise catalog,
- **When** the user creates a new exercise "سحب ظهر واسع" under category "الظهر",
- **Then** the new exercise is saved with `isCustom = true` and appears in exercise selection.

### AC-4: Workout Session Initialization
- **Given** active participants A, B, and C,
- **When** the user starts a session selecting only A and B,
- **Then** a new `WorkoutSession` is created with current timestamp, associated only with A and B.

### AC-5: Round Logging with Decimal KG & Reps
- **Given** an active session with participant A on exercise "بنش برس بالبار مستوي",
- **When** set 1 is logged for participant A with 22.5 kg and 10 reps,
- **Then** a `SetLog` record is stored with `setNumber = 1`, `weightKg = 22.5`, `reps = 10`, linked to participant A, the exercise, and the session.

### AC-6: Fast Step Controls (+/- 2.5 kg for Weight, +/- 1 for Reps)
- **Given** a set entry row with current weight 20.0 kg and 10 reps,
- **When** the user taps `+2.5` on weight and `+1` on reps,
- **Then** the displayed weight increments to 22.5 kg and reps increment to 11; and tapping `-2.5` on weight decrements it back to 20.0 kg.

### AC-7: Direct Numeric Keyboard Input for Decimal Weights
- **Given** a set entry row with weight 20.0 kg,
- **When** the user taps the weight display value and types "22.5" on the numeric keypad,
- **Then** the input accepts the decimal string and parses it to 22.5 kg without requiring step-button clicks.

### AC-8: Previous Set Pre-fill and Decrement Capability
- **Given** participant A has recorded a previous round or previous session with 60.0 kg and 10 reps on "سكوات بالبار",
- **When** participant A opens that exercise for a new round,
- **Then** the initial fields pre-fill automatically with 60.0 kg and 10 reps, and the user can decrement the pre-filled weight (e.g. to 57.5 kg) using the `-2.5` button.

### AC-9: Historical Performance Lookup by Session
- **Given** a completed past session where participant A logged 80.0 kg x 6 reps on "سكوات بالبار",
- **When** the user inspects that session in History,
- **Then** the session card shows the date, participants, exercise, and the recorded 80.0 kg x 6 reps in a structured table.

### AC-10: Session Completion
- **Given** an active workout session with multiple logged sets,
- **When** the user taps "إنهاء التمرين" (Finish Workout),
- **Then** the session's `endTime` is updated and the session transitions to completed status.

### AC-11: Input Validation for Weight and Reps
- **Given** an active set entry row,
- **When** a user attempts to enter a non-positive weight (`weightKg <= 0.0`), a non-numeric string, or non-positive reps (`reps <= 0`),
- **Then** the system rejects the input or clamps it to the minimum allowed value (0.5 kg, 1 rep) and does not persist an invalid set.

### AC-12: Input Validation for Names
- **Given** the participant creation or exercise creation dialog,
- **When** the user attempts to save an empty string or whitespace-only name,
- **Then** the operation is rejected with a validation error and no database record is created.

### AC-13: Soft Deletion of Participant with Existing History
- **Given** participant "أحمد" has recorded rounds in past sessions,
- **When** the user deletes "أحمد",
- **Then** "أحمد" does not appear in the participant selection list for starting new sessions, AND all of "أحمد"'s past sessions, rounds, and weights remain fully visible under his name in the History screen.

### AC-14: In-Session Round Modification and Deletion
- **Given** an active session with round 1 recorded for participant A (50.0 kg, 10 reps),
- **When** the user modifies round 1 to 52.5 kg or deletes round 1,
- **Then** the `SetLog` record is updated in the database accordingly or deleted, and round numbers re-index cleanly.

### AC-15: Active Session Recovery After App Restart
- **Given** an ongoing session with logged sets that has not been explicitly finished,
- **When** the app process is terminated and reopened,
- **Then** the app identifies the unclosed session, restores its state, and displays the previously recorded sets.

### AC-16: Session-Organized Tabular History View
- **Given** multiple past completed sessions,
- **When** navigating to the History screen,
- **Then** sessions are listed chronologically as cards/tables showing session date, start/end time, participating friends, and their respective weights (KG) and reps per exercise.

---

## 7. Open Questions
- None.

---
Status: draft
