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

### Feature packages inside `:app`

`feature.selection` (walking skeleton) and `feature.calculator` exist. `feature.history` and `feature.onboarding` exist too. Planned: `feature.onboarding`, `feature.widget`. Rule: a feature package never imports another feature package; shared things move to `core`. Splitting into modules later is a move, not a redesign.

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
| Compose UI | Compose UI test (`ui-test-junit4`) for the sheet states, plus `ActivityScenario` tests for the real `PROCESS_TEXT` entry point | `:app` androidTest (needs an emulator or device: `./gradlew :app:connectedDebugAndroidTest`) |
| OEM behaviour | Manual matrix: menu placement, `ACTION_PROCESS_TEXT` on at least two OEMs | real devices |

## 3. First TDD cycles (done, each committed green)

1. **Number grouping** (`NumberFormatterTest`): `1234567.89` → `12,34,567.89` (Indian) and `1,234,567.89` (international). Red: `NotImplementedError`. Green: reverse-chunk grouping. Refactor: none needed.
2. **Arithmetic** (`CalculatorTest`): `1,20,000 + 85,000` = 205000, `2 + 3 × 4` = 14, operators `x * / - −`, `1 ÷ 3` to 10 significant digits. Red: stub throws. Green: tokenise + two-pass precedence on `BigDecimal`.
3. **Messy text** (`CalculatorTest`): `Order Zomato ₹1,200 + ₹450 delivery` → 1650, read as `₹1,200 + ₹450`, ignored words, currency flag; standalone `x` is an operator but `xerox` is a word. Red: values and fields wrong. Green: single-pass scanner. Refactor candidate: split scanner and evaluator when ambiguity handling lands.

Then tokens, test-first: contrast and type-scale tests (red on placeholder values, green on the design values).

## More cycles done (each red, then green, committed)

4. **Nothing to calculate:** no number (empty, blank, `₹`, lone operators) → `CalcResult.NothingToCalculate`. A single number is a valid result.
5. **Errors:** `÷ 0` → `Error(DIVIDE_BY_ZERO)`; stray, leading or trailing operators and adjacent plain numbers → `Error(UNREADABLE)`. Scanner and evaluator split into `Scanner`/`Token`/`Calculator`.
6. **Percent:** `a × p%`, `a ÷ p%` use p/100; `a ± p%` applies p% of the running total; bare `p%` is p/100; a space before `%` is allowed.
7. **Ambiguity:** an amount directly followed by a percent (`Loan ₹2,50,000 12%`) → `Ambiguous` with readings ordered most likely first (× then +), rest of the sum kept, ignored words and currency flag carried. One gap only; more is unreadable.

## Walking skeleton: select text → parser → sheet → Replace/Copy (done)

8. **Negative formatting** (`NumberFormatter`): `−12,345` with U+2212. A real grouping bug found while planning the presenter.
9. **`SelectionPresenter`** (JVM tests): parser result → one of the five sheet states. Result always grouped; Copy plain digits; Replace keeps ₹/grouping only if the source used them; no Replace text in read-only fields; ambiguous readings with the most likely preselected.
10. **`SelectionSheet`** (Compose, tested on a device): states A–E from the design board, tokens only, live regions, 48 dp targets.
11. **`SelectionActivity`** (`ACTION_PROCESS_TEXT`, translucent): Replace returns the text via the activity result, Copy writes the clipboard, Close cancels, missing text shows "nothing to calculate". Verified on an API 34 emulator: 14 instrumented tests, the sheet renders over the home screen, and the scrim measures exactly 32%.

Not in the skeleton yet: Edit expression and "Type it myself", the lakh/crore word line, History saving, and error screens showing the "Read as" expression. Still unverified: how "Calculate" appears inside real apps' selection menus (needs a device pass on at least two phone makers).

## Main calculator screen (done, test-first)

12. **Word line** (`AmountWords`): `2.05 lakh`, `1 crore` (99.999 lakh rounds up to a crore), `1,234.57 crore`, `−2.05 lakh`; nothing under one lakh.
13. **Leading minus** (parser): `−5 + 3`, `−₹1,200 + ₹450` keep the sign; only the very first minus is a sign, `5 − −3` stays unreadable. Needed so a negative result can carry on.
14. **`CalculatorReducer`** (JVM): what each of the 20 keys does to the raw expression: no doubled leading zeros, one decimal point per number, an operator replaces the previous one, percent only after a digit, 15-digit cap per number, `=` commits a plain-digit result (real minus kept) and leaves an uncomputable expression alone.
15. **`CalculatorDisplay`** (JVM): grouped and spaced expression, live result (a trailing operator shows the running value), word line, error state with `=` disabled.
16. **`CalculatorScreen`** (Compose, 7 device tests): empty state, 4 × 5 keypad with the design's key styling and 72 × 64 dp minimum, live result that shrinks to 32 sp before wrapping, error line with icon and live region. Checked on the emulator in light and dark; matches boards 5A and 5D.

Decisions and differences from the board:
- **Word line decimals:** the spec text says one decimal, the board's own examples show two (`2.05 lakh`). Two decimals, trailing zeros trimmed.
- **Placeholder** reads "Type numbers" until pasting into the expression exists; the board says "Type or paste numbers".
- **Not built yet on this screen:** History and More buttons (hidden rather than dead), the hint chip (needs storage to count three showings), editing the expression as a real text field and pasting, hold-to-repeat on backspace, the wide-screen layout (display left, keypad right, from 600 dp), scientific notation over 15 digits. The emulator named `New_Device` is about 1080 dp wide, so on it the phone layout just stretches; use `adb shell wm density 420` to see the phone design.

## History (done, test-first)

17. **`HistoryRepository`** (JVM, fake store): newest first, the same sum twice in a row kept once (same source), capped at 500 with the oldest dropped, delete, clear all.
18. **`HistoryGrouping`** (JVM): Today, Yesterday, then dated groups, in the viewer's time zone (an entry at 00:30 India time is "today" there but "yesterday" in UTC); uses `Calendar`, not `java.time`, because minSdk is 24.
19. **`HistoryRecorder`** (JVM): what is worth saving. A sum from `=` is saved as shown; a lone number, a trailing-operator leftover, a failed sum, nothing-to-calculate and errors are not. A selected-text result is saved with its reading and "From selected text"; for an ambiguous one, the reading that was chosen.
20. **`SqliteHistoryStore`** (device tests): the framework's own SQLite, so no Room, no annotation processing and no new dependency. Ordered by insertion id so a clock change cannot reshuffle the list. ₹ and the real minus round-trip.
21. **`HistoryScreen`** (device tests): day headers, rows at least 72 dp, caption on selected-text rows, tap reuses the result, long-press Copy/Delete (also exposed as TalkBack custom actions), Clear all behind a confirmation, empty message.
22. **Wiring** (device tests): `=` saves, the History button on the calculator, tap-to-reuse puts the result (with the real minus) into the expression, Back returns to the calculator untouched, the sheet saves a clear result even if it is only closed (as the spec says), an ambiguous one when Replace or Copy is used.

Decisions to know about:
- **Privacy:** History holds what people calculated, often money, and the sheet promises "Nothing is sent anywhere", so `history.db` is excluded from cloud backup (both the Android 11 and Android 12+ rule files). A direct phone-to-phone transfer still carries it.
- **Database on the main thread:** loading and saving touch at most 500 small rows, so it is done inline for now. If it ever shows up as jank, move the store calls behind a background dispatcher; the repository is the only caller.
- **Reuse replaces the whole expression** with the saved result; it does not append.
- **Not done:** the More menu, the hint chip, "Settings" (number format, theme), the wide-screen layout, an editable expression field.

## Onboarding, hint chip and More menu (done, test-first)

23. **`OnboardingProgress` and `HintChip`** (JVM): first launch shows onboarding until it is finished or skipped; the chip shows on at most three launches (one question per launch uses up a showing), and never again once dismissed or used.
24. **`OnboardingScreen`** (device tests): step 1 with a drawn selection toolbar (three dots ringed and numbered 1, "Calculate" ringed and numbered 2, described once for TalkBack), step 2 with a real selectable practice line; Skip on both, Next, Back, Done.
25. **`AppRoot`** (device tests): onboarding on first launch, the hint chip on the empty calculator (dismiss, or tap to open the how-to), and a More menu with "How to use Calculate" and "About". The calculator keeps what was typed while onboarding is open.

A bug report ("Next and Skip do nothing") could not be reproduced on the emulator, but looking for it found real layout defects, now fixed with tests: with the largest text size or a short screen Next/Done scrolled out of reach (the buttons are now pinned, only the explanation scrolls), the demo toolbar labels wrapped, badge 1 was clipped by a shadow, and a trailing space in a string resource was trimmed away (quote strings that need one).

Left out on purpose: **Settings** (number format Indian/International, theme) is not in the More menu because nothing behind it exists yet; a menu item that does nothing is worse than no item.

Tips for testing by hand: `connectedDebugAndroidTest` uninstalls the app and drives screens on the emulator that do not respond to taps, so reinstall with `./gradlew :app:installDebug` and clear data (`adb shell pm clear in.simply.hisaab`) to see the first-launch flow. A single test class: `-Pandroid.testInstrumentationRunnerArguments.class=<full.class.Name>` (the Android task has no `--tests`).

## Device findings (API 34 emulator, 2026-11-18)

Checked by hand in two hosts: Android Settings search (a native EditText) and Chrome (a web page with a textarea and plain paragraphs).

| Check | Native field (Settings) | Chrome |
|---|---|---|
| "Calculate" appears in the selection toolbar | Yes, under the three dots, after "Read aloud" | Yes, under the three dots, after "Read aloud" |
| Sheet shows the right reading (`Lunch 320 + 40` → 360; `Lunch ₹320 + tip ₹40` → ₹360 with "Lunch, tip" ignored) | Yes | Yes |
| **Replace writes the answer back into the field** | **Yes** (field became `360`) | **No: sheet closed, textarea unchanged** |
| Read-only text: ambiguous banner, Copy primary, no Replace, note shown | not tried | Yes (`Loan ₹2,50,000 12%`) |
| Copy puts plain digits on the clipboard | not tried | Yes (pasted `30000`) |

- **Discoverability is confirmed as the main risk.** In Chrome the first toolbar for a half-selected word did not show the overflow at all; it appeared only after "Select all". Both toolbars hide Calculate behind the three dots. The onboarding screens are worth shipping with the first build.
- **Replace in Chrome: root cause found, not fixable in our code.** Chrome's own page events were logged during the flow. The activity returns `RESULT_OK` with `₹360` and Chrome is correctly named as the caller. But the moment Chrome launches any text-processing activity it fires `blur` on the textarea and collapses its selection (0–20 to 20–20). When the result arrives there is no selection, so Chrome applies nothing (no `input` event, value unchanged). A native `EditText` keeps its selection, so Replace works there.
  - Ruled out: a repaint glitch (the DOM value really is unchanged), and window focus (making the sheet window non-focusable did not stop the blur; that experiment was reverted).
  - **Product consequence:** Replace is reliable in native fields and unreliable in web pages. Copy works everywhere.
  - **Decision: option (b), implemented.** Replace returns the answer to the host and also puts the same text on the clipboard, with a short "Also copied to clipboard" toast. Where the host ignores the result (Chrome), one paste finishes the job; checked by hand: Replace, then select the line and paste gave `₹360`. Trade-off accepted: the clipboard is overwritten even when Replace worked. Covered by an instrumented test (clipboard read back with the shell's permission, because Android blocks background clipboard reads for apps).
  - Still to check: one more web host and one more phone maker.
- **Test hygiene:** `connectedDebugAndroidTest` uninstalls the app when it finishes, so reinstall (`./gradlew :app:installDebug`) before checking by hand. This cost one false alarm ("Calculate is missing") during this pass.

## Next cycles (backlog, in order)

1. Unary minus in input (`−5 + 3`); negative results already format correctly.
2. Word line (lakh/crore, from 1 lakh), currency formatting rules (₹, trailing zeros trimmed, 10 significant digits, half-up shown).
3. "Too large" error and scientific notation over 15 digits; Copy output gives plain digits.
4. Words as operators (`into`, `plus`, `minus`) and `@` read as ×.
5. Devanagari and Tamil digits on input; spoken form for TalkBack ("two lakh five thousand").
6. A corpus-driven test: one file of real messy strings with expected results, one parameterised test.
7. Settings (Indian/International grouping, theme) and then the item in the More menu; the home-screen widget; the wide-screen layout; an editable expression field with paste; Hindi and Tamil strings.
8. Onboarding, then the widget (the first second consumer, so extract `feature:*` modules then).

## Assumptions and risks

- **MVP timeline assumed 4–6 weeks** (the spec talks about "weeks 3 to 6"). Please confirm.
- Package `in.simply.hisaab`, minSdk 24, target/compile SDK 37, taken from the existing project.
- **Design gap:** the board's example lists only "Zomato, delivery" as ignored words for `Order Zomato …`. A deterministic rule ignores every non-numeric word, so "Order" is ignored too. Confirm or give a rule for leading words.
- **Risk, OEM menus:** Calculate may sit under "More" or not appear in some apps (custom text views, WebViews). Needs real-device checks early.
- **Risk, boundary erosion** inside `:app` under deadline pressure. Mitigation: the "no feature imports another feature" rule, and extract at the first second consumer (widget).
- **Parser surface:** errors, percent and ambiguity are in. Unary minus, the word line and spoken forms are still open (backlog 1–5); the result types the UI needs (success, ambiguous, nothing, error) already exist.
- Toolchain: Gradle daemon is pinned to JDK 25 by `gradle/gradle-daemon-jvm.properties`; CI uses JDK 25 to match.
- Out of scope by constraint: scientific mode, camera input, AI features.
