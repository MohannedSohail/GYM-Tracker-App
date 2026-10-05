---
name: intent-interview
description: Interviews a non-engineer and writes an intent.md, used when someone describes a new feature or problem before any spec exists.
---

# Intent Interview Skill

Use this skill whenever someone describes a new feature, improvement, or problem before any formal spec exists.

## Instructions

1. **Read `AGENTS.md`**:
   - Always read and respect the project constitution in `AGENTS.md` to understand project scope, architecture, stack, and hard constraints.

2. **Conduct the Interview**:
   - Ask **one question at a time**.
   - Keep questions simple, friendly, and accessible to non-engineers.
   - Stop after at most **8 questions** (or earlier if all required information is gathered).

3. **Draft the Intent Document**:
   - Use `intent/_template.md` as the template structure.
   - Save the result to `intent/<slug>.md` (using a concise, descriptive kebab-case slug for the filename).
   - Write the content using the author's own words and phrasing rather than engineering jargon.
   - Fill out each section:
     - `Problem`
     - `Who is affected`
     - `Desired outcome`
     - `Constraints`
     - `Non-goals`
     - `Open questions`

4. **Status and Approvals**:
   - End the document with any unresolved items under `## Open questions` and a final line `Status: draft`.
   - **Never mark the intent document as approved**—only the human can approve an intent document.
