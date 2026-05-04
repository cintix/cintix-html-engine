# Repository Guidelines

## Project Structure & Module Organization
This project is a Java template engine that parses custom HTML-like tags, executes tag logic, and injects runtime variables.
- `src/dk/cintix/html/engine/`: engine entry points and processing flow.
- `src/dk/cintix/html/engine/tags/`: custom tag contracts and implementations.
- `src/dk/cintix/html/engine/tags/base/`: base abstractions such as `HTMLTag`.
- `resources/`: template inputs used for local/manual verification.
- `test/`: automated test suite (expand this; currently sparse).
- `build/`, `dist/`: generated output from Ant builds.

## Build, Test, and Development Commands
Run from repository root:
- `ant clean`: remove build artifacts.
- `ant compile`: compile Java 17 sources.
- `ant test`: run automated tests.
- `ant jar`: package `dist/cintix-html-engine.jar`.
- `ant default`: full build + test target.

Use the existing harness class (`HTMLEngineTest`) only for quick manual checks; do not rely on it as primary validation.

## Coding Style & Naming Conventions
- Java source/target: `17`.
- Indentation: 4 spaces, no tabs.
- Naming: classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- Keep tag classes small and single-purpose (one behavior per tag class).
- Prefer clear error paths for invalid template syntax and missing variables.

## Performance & Optimization Priorities
- Minimize repeated string concatenation in hot paths; prefer buffered/streamed assembly.
- Avoid reparsing templates when inputs are unchanged; add caching where safe.
- Keep reflection and dynamic class loading outside tight loops.
- When optimizing, include before/after measurements and sample template size used.

## Testing Guidelines (AAA Required)
All new tests should follow Arrange-Act-Assert explicitly.
- Arrange: build input template, variable map, and tag registry.
- Act: call `HTMLEngine.process(...)` once.
- Assert: verify exact output, whitespace-sensitive cases, and failure behavior.
- Test names: `should_<expectedBehavior>_when_<condition>()`.
- Place tests under `test/` mirroring package paths.

## Commit & Pull Request Guidelines
- Use short imperative commits with clear scope (example: `optimize variable substitution path`).
- Keep commits focused: performance, refactor, or tests, not all mixed.
- PRs must include: summary, rationale, test evidence (`ant test`), and output examples for parser changes.
