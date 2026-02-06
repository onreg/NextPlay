---
name: plan-implementer
description: Execute engineering tasks strictly by following a provided implementation plan (usually a markdown "Step 1/2/3" plan or task prompt). Use when the user provides a plan and wants Codex to implement the task step-by-step, completing each step fully before starting the next, without asking questions.
---

# Plan Implementer

Implement the task by executing the plan steps in order. Do not ask clarifying questions; make reasonable assumptions and proceed. Do not stop until every step is completed, unless hard-blocked by missing access/credentials or unavailable external systems.

## Obey repo rules

- Follow any applicable `AGENTS.md` instructions for coding conventions and verification requirements.
- Do not add comments to source files unless explicitly required by the user.
- If repository interactions are needed (issues/PRs/branches), use the GitHub MCP tools rather than running `git` commands in a shell.

## Inputs and plan discovery

Use the user-provided content as the source of truth:

- If the user provides a dedicated plan file/path, read that file.
- Else, treat the task prompt’s "Implementation Steps" section as the plan (the plan format is consistent and stable).

## Execute strictly step-by-step

For each step, in order:

1. Start the step (and only this step).
2. Implement exactly what the step requires in the codebase.
3. If the current step explicitly includes validation (tests/lint/static analysis), run only those checks for this step’s scope and fix failures until it passes.
4. Mark the step complete and only then move to the next step.

Do not run validations early. Run them only when the plan step says to do so.

Rules:

- Do not reorder, merge, or skip steps.
- Do not implement “future steps” early, even if it seems efficient.
- If a later step depends on earlier prep work that is not in the plan, implement the smallest change needed to unblock the current step and keep it clearly aligned with that step’s outcome.

## Handling ambiguity without questions

When details are missing, choose the least risky, most conventional option that satisfies the plan and existing repo conventions. Document assumptions in the final handoff summary (not as source comments).

If blocked:

- First attempt to unblock by searching the repo, reading nearby code, and following existing patterns.
- Only stop when you are truly hard-blocked (e.g., missing credentials, no access to required external system, or a tool cannot run in this environment).

## Progress tracking (use `update_plan`)

Use the `update_plan` tool to mirror the plan’s steps and enforce sequential completion:

- Exactly one step `in_progress` at a time.
- Move a step to `completed` only after verification passes for that step.
- Do not jump steps from `pending` to `completed`.
