# Robot Framework API tests

API test project for the Tester Calculator. Specification: <http://localhost:8080/docs/>, contract: <http://localhost:8080/openapi.yaml>.

## Setup

```bash
cd tests/robot
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

If `pip install` fails on a very new Python, create the venv with a Homebrew 3.13 instead:
`/opt/homebrew/opt/python@3.13/bin/python3.13 -m venv .venv`.

## Run

Start the application first (`./gradlew bootRun` from the repository root), then:

```bash
.venv/bin/robot -d results tests                 # whole suite
.venv/bin/robot -d results tests/operations.robot
.venv/bin/robot -d results -t "Invalid Equation Is Rejected" tests
CALC_BASE_URL=http://localhost:8080 .venv/bin/robot -d results tests
```

Reports land in `results/` (`report.html`, `log.html`).

## Layout

| Path | Purpose |
|---|---|
| `variables.py` | `BASE_URL` (overridable with `CALC_BASE_URL`), user credentials, wait timeouts |
| `resources/api.resource` | Sessions per user, one keyword per endpoint, `Wait Until Operation Completes`, `Submit And Wait` |
| `tests/*.robot` | Suites grouped by area; add new `.robot` files here |

Sessions are created per suite with `Create Session For User ${ALICE}` (alias = username) or `Create Anonymous Session` (alias `anon`). Endpoint keywords take `expected_status=any` by default so a test can assert the status itself, or pass the expected code to fail fast.
