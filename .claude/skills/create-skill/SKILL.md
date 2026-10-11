---
name: create-skill
description: 'Create or change an agent skill for Barista the right way: one body in docs/reference/<name>.md, thin mirrored stubs in .claude/skills and .cursor/skills, a pushy bilingual description, a line in AGENTS.md. Trigger whenever the user wants to create, add, write, rename or edit a skill — English "create a skill", "new skill", "add a skill", "make this a skill", "turn this into a skill"; Russian "создай скилл", "новый скилл", "добавь скилл", "сделай из этого скилл", "напиши скилл". Also trigger when creating or modifying any SKILL.md under .claude/skills/ or .cursor/skills/.'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/skill-creation.md`.
