---
name: task-spec-clarifier
description: Read a Task Brief .md file, optionally fetch referenced Jira and Figma, ask clarifying questions, then write a copy-pastable Task Prompt spec with acceptance criteria for implementation planning.
---

# Task Spec Clarifier

## Purpose

Transform a single Task Brief Markdown document into:
1) a prioritized list of clarifying questions (focused on ambiguities, edge cases, and missing decisions), then
2) a finalized, copy-pastable "Task Prompt" specification with explicit acceptance criteria.

This skill is NOT for writing code and NOT for producing an implementation plan.
It only clarifies requirements and produces a crisp spec for a separate planning agent.

## Primary input

- Path (or repo link) to a Task Brief markdown file (".md").

The Task Brief contains:
- the problem description and expected change
- optional Jira links/keys
- optional Figma links/nodes

If Jira/Figma links are NOT provided, use only the Task Brief content and do NOT invent requirements.

## Tools (conditional)

Only use tools when links are present and accessible.

- Jira: use "mcp__atlassian" to fetch issue details.
- Figma: use "mcp__figma" to fetch frames/nodes, copy text, and states.

If a tool call fails (permissions, missing link, timeout), explicitly state what is missing and ask the user to paste the relevant content:
- Jira: summary, description, acceptance criteria, screenshots, relevant comments, linked issues
- Figma: frame screenshots, node IDs, exported images, copy/states description

## Output files (required)

Persist outputs as markdown files in the repository:

- Questions file: "docs/tmp/<slug>-clarifying-questions.md"
- Task prompt file: "docs/tmp/<slug>-task-prompt.md"

The directory "docs/tmp" must exist. If it does not, create it.

### Slug rules

- If at least one Jira key is discovered: use the first key in lowercase, e.g. "mobp-9188"
- If multiple Jira keys: "multi-<firstkey>-<count>", e.g. "multi-mobp-9188-3"
- If no Jira keys: "adhoc-<short-topic>" derived from the Task Brief title (lowercase, hyphenated)

Always print the final file path(s) in chat after writing.

## Language and formatting

- Match the language of the Task Brief. If mixed/unclear, default to English.
- Use straight double quotes only: " ".
- Avoid long dashes. Use "-" or ":".

## Workflow

### Phase 0: Read the Task Brief (always)

1) Open and read the provided Task Brief .md file.
2) Extract:
    - title and short summary
    - requirements stated in the text
    - constraints (platforms, release targets, backward compatibility, analytics, feature flags)
    - all links found in the text (Jira, Figma, docs)

### Phase 1: Fetch referenced context (only if links exist)

#### Jira (for each ticket key or URL found)
Fetch and read:
- summary/title
- description and acceptance criteria
- priority, status, components/labels
- linked issues/subtasks
- attachments (if accessible)
- recent comments (focus on decisions and scope changes)

#### Figma (for each link/node found)
Open and extract:
- relevant frames/screens
- exact UI copy text
- variants/states (default, loading, error, empty, disabled)
- interactions/flows implied by the design

### Phase 2: Normalize and cross-check (silent preparation, then summarize)

Build a compact internal understanding:
- "What changes" (behavior, UI, copy, data)
- "Where it changes" (screens/flows/modules implied, but do NOT propose architecture)
- "What can go wrong" (edge cases, state transitions, existing users)
- Cross-check Jira vs Figma mismatches:
    - copy differences
    - field names/options mismatch
    - rules/validation mismatch
    - missing screens/states
- Identify missing decisions that block implementation

### Phase 3: Clarifying questions (first output)

Create or overwrite:
- "docs/tmp/<slug>-clarifying-questions.md"

The file must contain:

# Clarifying questions

## Inputs
- Task Brief: <path to the .md provided by the user>
- Jira: <keys/URLs discovered or "none">
- Figma: <links/nodes discovered or "none">

## Context summary
- 3 to 8 bullets, no large quotes

## Questions (max 12, prioritized)
For each question use:

### Q<n>. <question>
- Type: BLOCKER | NON-BLOCKER
- Why it matters: <1 sentence>
- Options:
    - A) ...
    - B) ...
    - C) ...
- Default assumption if unanswered: <explicit>

## Detected ambiguities, conflicts, or gaps
- bullets (Jira vs Figma mismatches, missing mapping, unclear rules)

## Proposed assumptions (to confirm)
- bullets (only if needed)

In chat, output ONLY:
- The file path you wrote
- A short note: "Reply with answers in the same Q1..Qn format."

Do NOT include the full questions list in chat if it is already written to the file.

### Phase 4: Final spec (after user answers)

After the user replies with answers to Q1..Qn:
1) Incorporate answers and resolve ambiguities.
2) Produce a single spec section titled exactly "## Task Prompt".
3) Ensure "Open questions" is empty. If not, mark each as "BLOCKER" or "NON-BLOCKER".

Create or overwrite:
- "docs/tmp/<slug>-task-prompt.md"

File content template:

## Task Prompt

### Title
- <short, specific>

### Source links
- Task Brief: <path>
- Jira: <list keys/URLs or "none">
- Figma: <list URLs/nodes or "none">
- Other: <docs if present>

### Context
- 3 to 6 bullets: user problem, why change, current behavior vs desired behavior

### Goals
- bullets, measurable where possible

### Non-goals
- bullets, explicitly out of scope

### User experience
- Step-by-step user flow
- Include exact UI copy when relevant
- Include all states: default, loading (if applicable), error, empty, disabled
- Define behavior for existing users and previously saved values

### Functional requirements
- Numbered list
- Validation rules, conditional visibility, precedence rules
- Explicitly list edge cases and expected outcomes

### Data and mapping
- Define each data point:
    - name, type, allowed values, nullability, default
- Mapping table from UI options to stored value(s)
- Backward compatibility rules (existing users, nulls, migrations if applicable)

### API/contracts (if applicable)
- Request/response fields
- versioning assumptions
- error and retry behavior

### Analytics/observability (if applicable)
- events and properties
- success metrics
- logging/monitoring expectations

### Rollout
- feature flag / gradual rollout if needed
- risk mitigation and monitoring signals

### Acceptance criteria
- Bullet list, testable and explicit
- No vague phrasing like "should work"

### Test plan (high-level)
- Unit tests: what to cover
- Integration tests: what to cover
- UI tests: happy path + key edge cases

### Open questions
- Must be empty after clarification
- If not empty:
    - <BLOCKER> ...
    - <NON-BLOCKER> ...

Quality bar:
- Avoid implementation details (no "use X library", no module/class structure).
- Do not invent requirements. If unknown, keep it as an explicit open question or a confirmed assumption.
- If Jira/Figma are absent, base everything only on the Task Brief and user answers.

## Example invocations

- "Use task-spec-clarifier on docs/tmp/task-brief.md"
- "Here is a Task Brief .md. It contains Jira and Figma links. Ask clarifying questions and then write the Task Prompt."
- "This Task Brief has no links. Use only the description, ask questions, then produce acceptance criteria."
