#!/usr/bin/env python3
"""Remind the agent that a production change needs a test, right after it edits production Kotlin.

docs/rules/testing.md is loaded at session start, but by the time code is written it is far back in context.
This hook re-surfaces the rule at the moment it matters: an edit of `src/main/**/*.kt` (outside `ui/`, which is
covered by previews and screenshot tests) whose class has no `<ClassName>Test.kt` anywhere under `src/test`.

Advisory only: it never blocks a write and always exits 0.
"""

import json
import os
import sys

SKIPPED_DIRS = {".git", ".gradle", ".idea", "build", "node_modules", ".kotlin"}


def project_root(file_path):
    root = os.environ.get("CLAUDE_PROJECT_DIR")
    if root:
        return root
    marker = os.sep + "src" + os.sep + "main" + os.sep
    module_dir = file_path.split(marker)[0]
    current = module_dir
    while current and current != os.path.dirname(current):
        if os.path.exists(os.path.join(current, "settings.gradle.kts")):
            return current
        current = os.path.dirname(current)
    return module_dir


def has_test(root, class_name):
    expected = class_name + "Test.kt"
    for current, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs if d not in SKIPPED_DIRS]
        if expected in files and (os.sep + "src" + os.sep + "test" + os.sep) in (current + os.sep):
            return True
    return False


def main():
    payload = json.load(sys.stdin)
    file_path = payload.get("tool_input", {}).get("file_path", "")

    normalized = file_path.replace("\\", "/")
    if not normalized.endswith(".kt") or "/src/main/" not in normalized or "/ui/" in normalized:
        return

    class_name = os.path.splitext(os.path.basename(file_path))[0]
    if has_test(project_root(file_path), class_name):
        return

    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PostToolUse",
            "additionalContext": (
                f"REMINDER: {class_name} has no unit test. Rule: production change ⇒ test in the same change "
                f"(docs/rules/testing.md). Write/extend {class_name}Test.kt, then run ./gradlew moats "
                f"before claiming done."
            ),
        }
    }, ensure_ascii=False))


if __name__ == "__main__":
    try:
        main()
    except Exception:
        pass
    sys.exit(0)
