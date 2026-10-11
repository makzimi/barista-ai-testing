# Creating a Skill

Body of the `create-skill` skill. One body, many thin stubs.

```
docs/reference/<name>.md            ← the body: all real content (workflow, commands, examples)
.claude/skills/<name>/SKILL.md      ← Claude stub: frontmatter + "Read and follow docs/reference/<name>.md"
.cursor/skills/<name>/SKILL.md      ← Cursor stub: same pointer, Cursor frontmatter
```
Agents read skills from their own folders with their own frontmatter, but the content must not diverge — so it lives
in one doc. An always-on convention goes to `docs/rules/` and an `@` include in `AGENTS.md` instead.

## Steps

1. **Name**: kebab-case, action-oriented (`write-unit-tests`, `screenshot-tests`). Same name for both stub dirs.
2. **Body**: `docs/reference/<name>.md`. Reuse an existing doc if there is one.
3. **Claude stub** `.claude/skills/<name>/SKILL.md`:
   ```markdown
   ---
   name: <name>
   description: <what + when, pushy, EN + RU trigger phrases, file-based triggers>
   allowed-tools:
     - Bash(./gradlew *)
     - Bash(grep *)
     - Bash(find *)
   ---

   Read and follow `docs/reference/<name>.md`.
   ```
4. **Cursor stub** `.cursor/skills/<name>/SKILL.md`: `name`, `description`, `shortcut: /<name>`,
   `category` (`testing` | `code-quality` | `build` | `documentation`), `tags: [...]`; same one-line body.
5. **Wire it**: add a line to "Read on demand" in `AGENTS.md`.

## The description is the trigger

It is the only text an agent sees when deciding to load the skill. Agents under-trigger far more than over-trigger:
- Lead with what it does, then list real phrasings: "write tests", "cover with tests", "add a test for X".
- Bilingual: "напиши тесты", "покрой тестами", "добавь тест".
- Name contexts and files: "Also trigger when creating or modifying any `*Test.kt` under `src/test/`."

## Checklist

- [ ] Body in `docs/reference/<name>.md`, no content copied into stubs.
- [ ] Claude and Cursor stubs exist and point to the same body.
- [ ] Description: pushy, EN + RU, trigger contexts.
- [ ] `AGENTS.md` "Read on demand" updated.
