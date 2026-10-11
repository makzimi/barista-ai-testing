---
name: screenshot-tests
description: 'Add, verify, re-record and review Roborazzi screenshot tests for Barista: @ThemePreviews previews become goldens automatically, hand-written *ScreenshotTest with captureScreenshot, goldens in src/test/screenshots. Trigger when the user changes how something looks or asks about screenshots — English "screenshot test", "update the goldens", "re-record screenshots", "verify screenshots", "visual regression", "the screenshot test failed", "dark theme looks wrong", "add a preview"; Russian "скриншот-тест", "обнови эталоны", "перезапиши скриншоты", "проверь скриншоты", "упал скриншот-тест", "сломалась вёрстка", "тёмная тема", "добавь превью". Also trigger when changing any composable under ui/ or designsystem/, or files in src/test/screenshots.'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/screenshot-tests.md`.
