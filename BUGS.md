# Planted bugs — trainer reference

**Do not share this file with trainees.** `README.md`, the specification (`/docs/`) and `openapi.yaml` never mention it.

Every entry lists where the bug lives, what the specification promises, what the application actually does, how to reproduce it and the kind of test that should catch it. Spec references point at sections of `src/main/resources/static/docs/index.html`.

Difficulty: **obvious** — found by a single request against the spec; **medium** — needs a specific input or two users; **subtle** — needs careful reading of the spec, timing or boundary analysis; **UI** — only visible in the browser.

| # | Difficulty | Area | One-liner |
|---|---|---|---|
| 1 | obvious | API | `POST /api/operations` returns `200` instead of `201` |
| 2 | obvious | security | `DELETE /api/operations/{id}` needs no authentication and deletes anyone's operation |
| 3 | obvious | API | unknown operation id returns `400` instead of `404` |
| 4 | obvious | API | default priority is `LOW` instead of `MEDIUM` |
| 5 | medium | priority | `MEDIUM` and `LOW` delays are swapped |
| 6 | medium | parser | division of two whole numbers is truncated (`7/2 = 3`) |
| 7 | medium | parser | subtraction is right-associative (`10-2-3 = 11`) |
| 8 | medium | parser | trailing operator is silently ignored (`5+` evaluates to `5`) |
| 9 | medium | security | `GET /api/operations/{id}` returns other users' operations |
| 10 | medium | evaluation | division by zero finishes as `DONE` with `"Infinity"` / `"NaN"` instead of `FAILED` |
| 11 | subtle | validation | 201-character equations are accepted; over-long equations use the wrong error code |
| 12 | subtle | listing | `page` is treated as 1-based (`page=0` and `page=1` return the same items) |
| 13 | subtle | security | username and password checks are case-insensitive |
| 14 | subtle | API | `202` response has no `Retry-After` header |
| 15 | UI | calculator | pending result renders `Result: undefined` |
| 16 | UI | calculator | logout does not clear the stored credentials |

---

## 1. `POST` returns `200` instead of `201`

- **Where:** `api/OperationController.submit` — no `@ResponseStatus(HttpStatus.CREATED)`.
- **Spec:** section 6, `POST /api/operations` → `201 Created`; `openapi.yaml` only documents `201`.
- **Actual:** `200 OK` with the correct body.
- **Reproduce:** `curl -i -u alice:alice123 -H 'Content-Type: application/json' -d '{"equation":"1+1"}' localhost:8080/api/operations`
- **Test that catches it:** assert the exact status code of a successful submission.

## 2. `DELETE` is unauthenticated and ignores ownership

- **Where:** `config/SecurityConfig` — `.requestMatchers(HttpMethod.DELETE, "/api/operations/*").permitAll()`; `api/OperationController.delete` and `operation/OperationService.delete` never look at the caller.
- **Spec:** section 3 and 6 — every `/api/**` endpoint except `/api/health` requires credentials; deleting another user's operation is `404`.
- **Actual:** `DELETE` without any `Authorization` header returns `204`; bob can delete alice's operation.
- **Reproduce:** submit as alice, then `curl -i -X DELETE localhost:8080/api/operations/<id>` (no `-u`) → `204`; alice's `GET` then returns `400 NOT_FOUND` (see bug 3).
- **Test that catches it:** unauthenticated request to each endpoint expecting `401`; cross-user delete expecting `404`.

## 3. Unknown id returns `400` instead of `404`

- **Where:** `api/ApiExceptionHandler.notFound` — `@ResponseStatus(HttpStatus.BAD_REQUEST)`.
- **Spec:** section 6/7 — `404` with `{"code":"NOT_FOUND"}`.
- **Actual:** `400` with `{"code":"NOT_FOUND","message":"Operation … not found"}`. The body is right, the status is wrong. Affects `GET` and `DELETE`.
- **Reproduce:** `curl -i -u alice:alice123 localhost:8080/api/operations/00000000-0000-0000-0000-000000000000`
- **Test that catches it:** assert status `404` (not just the error code) for a random UUID.

## 4. Default priority is `LOW`

- **Where:** `api/dto/SubmitOperationRequest.priorityOrDefault` returns `Priority.LOW`.
- **Spec:** section 5 — omitted `priority` defaults to `MEDIUM`.
- **Actual:** response `priority` is `LOW`; combined with bug 5 the operation actually completes after 5 s, which is the documented `MEDIUM` delay, so timing alone will not reveal it — the `priority` field in the response will.
- **Reproduce:** `curl -u alice:alice123 -H 'Content-Type: application/json' -d '{"equation":"1+1"}' localhost:8080/api/operations` → `"priority":"LOW"`.
- **Test that catches it:** submit without `priority` and assert `priority == MEDIUM` in both the submission response and the `GET` body.

## 5. `MEDIUM` and `LOW` delays are swapped

- **Where:** `priority/PriorityDelays.delayFor` — `MEDIUM → 15 s`, `LOW → 5 s`. `HIGH → 1 s` is correct.
- **Spec:** section 5 — `MEDIUM` = 5 s, `LOW` = 15 s.
- **Actual:** `MEDIUM` operations take 15 s, `LOW` 5 s. `estimatedCompletionAt` and `retryAfterSeconds` are derived from the wrong delay too, so they are internally consistent with the bug.
- **Reproduce:** submit `{"equation":"1+1","priority":"LOW"}` and poll; `DONE` after ~5 s. Submit with `MEDIUM`; still `202` after 10 s.
- **Test that catches it:** per-priority timing test asserting `estimatedCompletionAt - createdAt` and/or measured completion time.

## 6. Whole-number division is truncated

- **Where:** `parser/Expr.Binary.divide` — when both operands are integral (and the divisor is non-zero) the quotient is cast through `long`.
- **Spec:** section 4 — real-number division, `7/2 = 3.5`.
- **Actual:** `7/2 = 3`, `1/3 = 0`, `-7/2 = -3`. `7.0/2` is also truncated (7.0 is integral); `7.5/2 = 3.75` is correct.
- **Reproduce:** submit `7/2` with `HIGH`, poll → `"result":3`.
- **Test that catches it:** the spec's own example table (`7/2 → 3.5`).

## 7. Subtraction is right-associative

- **Where:** `parser/Parser.parseExpression` — on `-` it returns `Binary('-', left, parseExpression())` instead of looping.
- **Spec:** section 4 — all binary operators are left-associative, `10-2-3 = 5`.
- **Actual:** `10-2-3` parses as `10-(2-3) = 11`; `10-2+3` parses as `10-(2+3) = 5` (should be 11). Addition, multiplication and division chains are fine (`8/4/2 = 1`).
- **Reproduce:** submit `10-2-3` → `11`.
- **Test that catches it:** associativity cases; the spec example table.

## 8. Trailing operator is ignored

- **Where:** `parser/Parser.parseExpression` / `parseTerm` — after consuming an operator, if the next token is `END` the parser returns the left operand.
- **Spec:** section 4 — `5+` is a syntax error (`400 INVALID_EQUATION`, position 2).
- **Actual:** `5+`, `5-`, `5*`, `5/` are accepted (`201`… well, `200`, see bug 1) and evaluate to `5`. Note `(5+)` is still rejected because the token after `+` is `)` not end-of-input.
- **Reproduce:** submit `5+` → accepted; result `5`.
- **Test that catches it:** the documented syntax-error table.

## 9. `GET /api/operations/{id}` ignores ownership

- **Where:** `operation/OperationService.get` — the `owner` parameter is unused.
- **Spec:** section 3 — another user's operation id behaves like an unknown id (`404`).
- **Actual:** bob can read alice's operation (`200`/`202`). Listing (`GET /api/operations`) *is* filtered by owner, so the inconsistency is between the two read paths.
- **Reproduce:** submit as alice, `curl -u bob:bob123 localhost:8080/api/operations/<id>` → `202`/`200`.
- **Test that catches it:** cross-user read expecting `404`.

## 10. Division by zero finishes as `DONE`

- **Where:** `parser/Expr.Binary.divide` returns the IEEE result; `operation/OperationService.complete` only maps thrown exceptions to `FAILED`, and nothing is thrown.
- **Spec:** section 4 — `1/0` finishes with status `FAILED` and `error {"code":"DIVISION_BY_ZERO"}`, `result: null`.
- **Actual:** status `DONE`, `error: null`, and `result` is the JSON **string** `"Infinity"` (`1/0`), `"-Infinity"` (`-1/0`) or `"NaN"` (`0/0`) — a type violation against the `number` schema as well.
- **Reproduce:** submit `1/0` with `HIGH`, poll → `{"status":"DONE","result":"Infinity",…}`.
- **Test that catches it:** division-by-zero test; schema validation of responses against `openapi.yaml`.

## 11. Equation length boundary off by one, wrong error code

- **Where:** `operation/OperationService.submit` — `equation.length() > MAX_EQUATION_LENGTH + 1`, and it throws `EquationException` (→ `INVALID_EQUATION`).
- **Spec:** section 4/7 — maximum 200 characters; longer equations are `400 VALIDATION_ERROR`.
- **Actual:** 200 and 201 characters are accepted; 202+ characters are rejected with `INVALID_EQUATION` (position 200) instead of `VALIDATION_ERROR`.
- **Reproduce:** 201-character equation such as `1+1+1+…` (padded with spaces) → accepted. 202 characters → `{"code":"INVALID_EQUATION",…}`.
- **Test that catches it:** boundary-value test at 200/201 characters, plus asserting the error `code`.

## 12. `page` is 1-based

- **Where:** `operation/OperationService.list` — `Math.max(page - 1, 0) * size`.
- **Spec:** section 6 — `page` is zero-based.
- **Actual:** `page=0` and `page=1` both return the first page; `page=2` returns the second. The `page` field in the response echoes the requested number, so the body looks right.
- **Reproduce:** submit 3 operations as carol, then `GET /api/operations?page=0&size=2` and `page=1&size=2` → identical `items`; `page=2&size=2` → the third operation.
- **Test that catches it:** paging test with `size` smaller than the number of operations asserting disjoint pages and `total`.

## 13. Password comparison is case-insensitive

- **Where:** `config/SecurityConfig.passwordEncoder` — `equalsIgnoreCase`; additionally `InMemoryUserDetailsManager` lower-cases usernames on lookup.
- **Spec:** section 3 — credentials are case-sensitive.
- **Actual:** `alice:ALICE123`, `alice:Alice123` and `ALICE:alice123` all authenticate; neither the username nor the password is case-sensitive.
- **Reproduce:** `curl -i -u alice:ALICE123 localhost:8080/api/me` → `200`.
- **Test that catches it:** negative authentication test with wrong-case password.

## 14. `202` without `Retry-After`

- **Where:** `api/OperationController.get` — builds the `202` body with `retryAfterSeconds` but never sets the header.
- **Spec:** section 6 and `openapi.yaml` — `202` carries `Retry-After: <seconds>`.
- **Actual:** header absent; body is correct.
- **Reproduce:** submit with `LOW`, `curl -i -u alice:alice123 localhost:8080/api/operations/<id>` → no `Retry-After` line.
- **Test that catches it:** header assertion on the pending response.

## 15. UI shows `Result: undefined` while pending

- **Where:** `static/js/app.js`, result-form submit handler — on `202` it reads `operation.result`, which does not exist on the pending body.
- **Spec:** section 8 — `PENDING`/`IN_PROGRESS` shows "Not ready yet" in `result-value`.
- **Actual:** `result-status` shows `PENDING`/`IN_PROGRESS` correctly but `result-value` shows the literal text `undefined`.
- **Reproduce:** submit with `LOW`, immediately click **Get result**.
- **Test that catches it:** UI test asserting the pending text.

## 16. Logout keeps the credentials

- **Where:** `static/js/app.js`, logout click handler — navigates to `/` without removing `calc.auth`/`calc.user` from `sessionStorage`.
- **Spec:** section 8 — logout clears credentials; Back must land on the login page.
- **Actual:** after logout, pressing Back (or opening `/calculator.html` directly in the same tab) shows a working calculator with the previous user still signed in.
- **Reproduce:** log in, click **Log out**, press Back.
- **Test that catches it:** UI test for logout followed by navigation to `/calculator.html` expecting a redirect to `/`; or checking `sessionStorage`.

---

## Behaviours that are *not* bugs (so trainers can answer questions)

- Results are JSON numbers rendered with Jackson defaults: integral doubles print as `82.0`; the spec says so and tells testers to compare numerically.
- `0.1+0.2` → `0.30000000000000004` — documented, intentional double arithmetic.
- `GET /api/operations/{id}` with a non-UUID id → `400 VALIDATION_ERROR` "Malformed request" (documented).
- Unknown `priority` value or malformed JSON → `400 VALIDATION_ERROR` "Malformed request" (documented).
- The `401` response has no `WWW-Authenticate` header, so browsers do not pop up a native login dialog; the spec does not promise the header.
- Operations live in memory and disappear on restart (documented).
