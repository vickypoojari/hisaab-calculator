# Hisaab: architecture and setup

Design source: Claude Design canvas "Hisaab — Calculator MVP UI/UX" (boards: design system, build spec, selection sheet, calculator, widget, onboarding).

## 1. Decision: parser-first, then a walking skeleton, extract modules on demand

Sequence chosen: **approach 3 → approach 2**, with only the structure that is justified today.

- **Parser-first (days 1–3).** `:core:parser` is pure Kotlin. JVM tests run in about a second, so strict TDD stays cheap. The engine is the product's moat and the part most worth proving. The spec's rules (Indian grouping, ₹, percent, ignored words, ambiguity) are all testable without a device.
- **Walking skeleton (day 4 onward).** One thin slice in `:app`: select text → `ACTION_PROCESS_TEXT` activity → parser → result sheet → Replace/Copy. It lives in feature packages inside `:app` with strict boundaries.
- **Not approach 1.** Eight modules and a tokens module before any code would spend the first days on scaffolding while the real risk goes untested: does "Calculate" appear in the selection menu on real phones, and do people find it? Modules drawn before code exist tend to be wrong. The one exception is `:core:designsystem`, which is justified now (below).
- **When "does Calculate show up and get used?" gets tested:** the skeleton targets a real phone by the end of week 1, on at least two OEMs (menu placement differs). Discoverability (onboarding) is measured from the first build that goes to testers, not at the end of the MVP.
- **Split trigger:** extract `feature:*` modules when a second consumer appears (the widget is the first one, since it needs history, the parser and the tokens) or when incremental build time hurts. Until then, packages only.

## 2. Setup

### Module graph (today)

```
:app ──► :core:designsystem      (Compose theme + tokens, Android library)
:app ──► :core:parser            (added when the skeleton wires it; not yet used by :app)
:core:parser        ──► (nothing; pure Kotlin/JVM)
:core:designsystem  ──► (nothing in this repo)
```

| Module | Why it exists | Who depends on it |
|---|---|---|
| `:app` | Android entry points, DI wiring, feature packages | – |
| `:core:parser` | Deterministic, on-device text → value engine. No Android, no network, so it is provable and reusable | `:app` (and the widget later) |
| `:core:designsystem` | Tokens + `HisaabTheme`. The app, the result-sheet activity and the Glance widget all need identical tokens, and token tests (contrast) run without the app | `:app`, later widget |

Allowed directions: `app → core:*`; `feature packages → core:*`; `core:parser` depends on no module and on **no Android or Compose artifact**; `core:*` never depends on `app` or on a feature. Enforced by the convention plugin: `hisaab.jvm.library` applies no Android plugin, so an Android import in the parser does not compile.

### Feature packages inside `:app` (planned, not yet created)

`feature.selection`, `feature.calculator`, `feature.history`, `feature.onboarding`, `feature.widget`. Rule: a feature package never imports another feature package; shared things move to `core`. Splitting into modules later is a move, not a redesign.

### Version catalog and convention plugins

- `gradle/libs.versions.toml`: single source for versions, libraries and plugin aliases (including the Gradle plugins used by `build-logic`).
- `build-logic/` (included build): `hisaab.jvm.library` (Kotlin/JVM 11, warnings as errors, explicit API) and `hisaab.android.library` (SDK levels, Compose on). `:app` keeps its own config until a second application module exists.
- `build-logic` itself targets Java 17 because AGP 9.x requires it; modules still compile to Java 11.

### Design tokens (`:core:designsystem`)

- Colour: light and dark roles from the board (`HisaabColors.Light/Dark`). Dynamic colour is off for calculator surfaces.
- Type scale: `HisaabTypeScale.Standard`, plus `LargeText` (number-xl 72, key label 36) reserved for large-text mode. Swap via `HisaabTheme(typeScale = …)`.
- Spacing and targets: `HisaabDimens` (4/8/12/16/24/32/48, 48 dp minimum target).
- Number-formatting rules live in `:core:parser` (`NumberFormatter`, `GroupingStyle`) because they are engine behaviour, not styling.
- Guarded by tests: every text pair ≥ 4.5:1, outlines and caution icon ≥ 3:1, in both themes; type floor 12 sp.

### Reserved slots (nothing built)

- **Voice:** 48 dp space left at the trailing edge of the expression row, the "Read as" header and the 4×1 widget (layout rule, no code yet).
- **Large text:** `HisaabTypeScale.LargeText` token set; the 4×4 keypad is a layout to add later.
- **Tamil/Hindi:** `res/xml/locales_config.xml` lists `en`, `hi`, `ta`; add `values-hi/` and `values-ta/`. Native digits are already planned for the parser input stage.

### CI

`.github/workflows/ci.yml`: job `parser` (JVM tests only, fast fail) then job `build` (unit tests, lint, debug assemble, report upload). It has not run yet because the repository has no remote; the same commands pass locally.

### Test stack

| Layer | Tool | Where |
|---|---|---|
| Parser, formatter | JUnit4 via `kotlin-test`, a growing corpus of messy strings | `:core:parser` (pure JVM) |
| Tokens | JUnit4, WCAG contrast function | `:core:designsystem` |
| ViewModels / UI state | JUnit4 + `kotlinx-coroutines-test` + Turbine (add with the first ViewModel) | `:app` unit tests |
| Compose UI | Compose UI test (`ui-test-junit4`, already in catalog) for the sheet states | `:app` androidTest |
| OEM behaviour | Manual matrix: menu placement, `ACTION_PROCESS_TEXT` on at least two OEMs | real devices |

## 3. First TDD cycles (done, each committed green)

1. **Number grouping** (`NumberFormatterTest`): `1234567.89` → `12,34,567.89` (Indian) and `1,234,567.89` (international). Red: `NotImplementedError`. Green: reverse-chunk grouping. Refactor: none needed.
2. **Arithmetic** (`CalculatorTest`): `1,20,000 + 85,000` = 205000, `2 + 3 × 4` = 14, operators `x * / - −`, `1 ÷ 3` to 10 significant digits. Red: stub throws. Green: tokenise + two-pass precedence on `BigDecimal`.
3. **Messy text** (`CalculatorTest`): `Order Zomato ₹1,200 + ₹450 delivery` → 1650, read as `₹1,200 + ₹450`, ignored words, currency flag; standalone `x` is an operator but `xerox` is a word. Red: values and fields wrong. Green: single-pass scanner. Refactor candidate: split scanner and evaluator when ambiguity handling lands.

Then tokens, test-first: contrast and type-scale tests (red on placeholder values, green on the design values).

## Next cycles (backlog, in order)

1. Nothing to calculate (no numbers) → `NothingToCalculate`.
2. Divide by zero and unreadable text (`12 + + / 5`) → typed errors.
3. Percent rules: `a × p%`, `a + p%`, bare `p%`.
4. Ambiguity: `Loan ₹2,50,000 12%` → two readings with values, most likely first.
5. Word line (lakh/crore), currency formatting, negatives with `−`, scientific over 15 digits.
6. Devanagari and Tamil digits on input; spoken form for TalkBack.
7. Replace/Copy output rules (plain digits for Copy).

## Assumptions and risks

- **MVP timeline assumed 4–6 weeks** (the spec talks about "weeks 3 to 6"). Please confirm.
- Package `in.simply.hisaab`, minSdk 24, target/compile SDK 37, taken from the existing project.
- **Design gap:** the board's example lists only "Zomato, delivery" as ignored words for `Order Zomato …`. A deterministic rule ignores every non-numeric word, so "Order" is ignored too. Confirm or give a rule for leading words.
- **Risk, OEM menus:** Calculate may sit under "More" or not appear in some apps (custom text views, WebViews). Needs real-device checks early.
- **Risk, boundary erosion** inside `:app` under deadline pressure. Mitigation: the "no feature imports another feature" rule, and extract at the first second consumer (widget).
- **Risk, parser surface:** malformed input is not yet handled (backlog 1–2); do not wire the parser to UI before those cycles are green.
- Toolchain: Gradle daemon is pinned to JDK 25 by `gradle/gradle-daemon-jvm.properties`; CI uses JDK 25 to match.
- Out of scope by constraint: scientific mode, camera input, AI features.
