# Agent micro-docs (index)

This folder contains short, context-specific rules. Do not read everything.
Read only documents whose triggers match your change.

## How to use

- Identify edited paths + main intent (ViewModel, tests, mapping, snapshot).
- Use the triggers table below.
- If multiple triggers match, read all matched docs (usually 1–3).

## Triggers table (paths/symbols)

- Editing `app/src/main/java/**/presentation/**/*ViewModel*.kt` or class extends `ViewModel`:
  - Read `viewmodel.md`
- Beore creating a new Kotlin source file:
  - Read `code-organization.md`
