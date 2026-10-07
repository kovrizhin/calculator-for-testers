# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A deliberately buggy training application for QA engineers: an asynchronous calculator with a REST API, a plain HTML/JS UI, an OpenAPI contract (`src/main/resources/static/openapi.yaml`) and a specification page (`src/main/resources/static/docs/index.html`). The contract and the specification describe the *intended* behaviour; the code deviates from them on purpose. Every planted bug is catalogued in `BUGS.md`.

Rules that follow from that purpose:

- **Do not fix a bug listed in `BUGS.md` unless explicitly asked.** If you change behaviour, update `BUGS.md`, `openapi.yaml` and `docs/index.html` together so the three stay consistent with each other (the docs are the source of truth the trainees test against).
- **No comments in source code** (Java, JS, HTML, CSS); the code must not hint at the planted bugs.
- Smoke tests in `src/test` must keep passing *with* the bugs in place; never add a test that asserts the buggy behaviour or the documented-but-unimplemented behaviour.
- This is a standalone single-module Spring Boot app; it does not use the Paradym platform stack (no JooQ, Liquibase, Kafka, outbox, Temporal, gRPC). Ignore those parts of the workspace rules here.

## Commands

```bash
./gradlew build                                   # compile + smoke tests
./gradlew test                                    # smoke tests
./gradlew test --tests "io.paradym.testercalculator.OperationFlowSmokeTest"
./gradlew bootRun                                 # http://localhost:8080
```

JDK 25 via Gradle toolchain (foojay resolver). No Docker or database required.

## Layout

- `parser/` — `Tokenizer` → `Parser` (recursive descent) → `Expr` AST evaluated lazily at completion time.
- `operation/` — `Operation` model, in-memory `OperationStore`, `OperationService` (submit/get/list/delete + delayed completion via the `ScheduledExecutorService` from `config/AsyncConfig`).
- `priority/` — `Priority` enum and `PriorityDelays` (priority → delay).
- `api/` — controllers, DTO records, `ApiExceptionHandler` producing `{code,message[,position]}` bodies.
- `config/SecurityConfig` — HTTP Basic, in-memory users, permit rules, JSON 401 entry point.
- `static/` — `index.html` (login), `calculator.html`, `js/app.js`, `css/style.css`, `docs/`, `openapi.yaml`.

Spring Boot 4 notes: Jackson 3 (`tools.jackson.databind.ObjectMapper`), `@AutoConfigureMockMvc` lives in `org.springframework.boot.webmvc.test.autoconfigure` and needs `spring-boot-starter-webmvc-test`.
