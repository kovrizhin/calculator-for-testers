# Tester Calculator

A small asynchronous calculator service built as a training target for QA engineers. It ships a REST API, a two-page web UI, an OpenAPI contract and a human-readable specification. Testers write API and UI tests against the specification and find where the application deviates from it.

## Requirements

- JDK 25 (the Gradle toolchain downloads one if it is missing)
- Nothing else: no database, no Docker

## Run

```bash
./gradlew bootRun
```

Then open <http://localhost:8080>.

| URL | What |
|---|---|
| `/` | Login page |
| `/calculator.html` | Calculator page |
| `/docs/` | Specification for testers |
| `/docs/api.html` | Swagger UI |
| `/openapi.yaml` | OpenAPI 3 contract |

Training accounts: `alice/alice123`, `bob/bob123`, `carol/carol123`.

## Quick API tour

```bash
curl -u alice:alice123 -H 'Content-Type: application/json' \
  -d '{"equation":"6+7+(3+4*5) * 3","priority":"HIGH"}' \
  http://localhost:8080/api/operations

curl -u alice:alice123 http://localhost:8080/api/operations/<id>
curl -u alice:alice123 'http://localhost:8080/api/operations?page=0&size=20'
curl -u alice:alice123 -X DELETE http://localhost:8080/api/operations/<id>
```

## Build and test

```bash
./gradlew build          # compile + smoke tests
./gradlew test           # smoke tests only
./gradlew test --tests "io.paradym.testercalculator.parser.ParserSmokeTest"
```

The repository intentionally contains only a handful of smoke tests. The full test suite is the trainees' job.
