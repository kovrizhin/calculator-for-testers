# Playwright UI tests

Browser tests for the Tester Calculator UI (login page and calculator page). Specification: <http://localhost:8080/docs/> (section 8 lists every `data-testid`).

## Setup

```bash
cd tests/playwright
npm install
npx playwright install chromium
```

## Run

```bash
npm test                      # headless chromium
npm run test:headed
npm run test:ui               # Playwright UI mode
npm run report                # open the last HTML report
npx playwright test tests/login.spec.ts
npx playwright test -g "invalid equation"
```

If the application is not already running on `http://localhost:8080`, the `webServer` entry in `playwright.config.ts` starts it with `./gradlew bootRun` (first start can take a minute). Point the tests elsewhere with `CALC_BASE_URL=http://host:port`.

## Layout

| Path | Purpose |
|---|---|
| `playwright.config.ts` | Base URL, chromium project, reporters, app auto-start |
| `src/users.ts` | Training accounts |
| `src/pages/LoginPage.ts`, `src/pages/CalculatorPage.ts` | Page objects; every locator is a `getByTestId` |
| `src/fixtures.ts` | `test` with `loginPage`, `calculatorPage` and `loggedIn` (already signed in as alice) fixtures |
| `tests/*.spec.ts` | Specs grouped by page; add new files here |

Import `test` and `expect` from `../src/fixtures` rather than from `@playwright/test` so the fixtures are available.
