# Play Store listing: draft v1

Status: DRAFT. A human must review before pasting into Play Console.
Claims below match what is built today (calculator, Calculate from selected text, History, onboarding).
Do NOT add widget, Settings, Hindi or Tamil claims until those ship.

## Store listing text

**App name (max 30):** Hisaab - Calculator
(19 characters; matches `app_name`. Check availability when you create the app in Play Console.)

**Short description (max 80):**
Select numbers in any app, tap Calculate. Lakh and crore ready. No internet.

**Full description (max 4000):**
Hisaab is a calculator that works where your numbers already are.

Got a message like "Lunch ₹320 + tip ₹40"? Select the text, tap the three dots (on some phones "More"), then tap Calculate. If the menu is long, scroll to find it. Hisaab reads the numbers, ignores the words, and shows the answer in a small box at the bottom. Tap Replace to put the result back, or Copy to use it anywhere.

WHAT YOU GET
• Calculate from selected text in chats, browsers, notes and more
• Shows you how it read your text, so you can check or fix it
• A simple calculator with big, easy keys for everyday sums
• Indian number format (12,34,567.89) with lakh and crore in words
• Understands +, −, ×, ÷ and percentages
• History of your answers, grouped by day. Clear it any time
• Light and dark themes

PRIVATE BY DESIGN
Everything is calculated on your phone. Hisaab has no internet access, no ads and no account. Your text and your history never leave your device.

HOW TO USE CALCULATE
1. Touch and hold a word and drag to select the numbers.
2. Tap the three dots ("More") in the menu that appears.
3. Tap Calculate. If you don't see it, scroll the menu.

If the sum looks wrong, tap Edit, fix it, and the answer updates straight away.

Questions or feedback? Write to the contact email on this page.

## Other listing fields
- Category: Tools
- Tags: Calculator, Tools (pick from the Play list)
- Contact email: vickypoojari681@gmail.com (shown publicly)
- Website: optional, skip
- Default language: English (India) en-IN

## Graphics (made, in graphics/ and screenshots/)
- Icon 512x512: graphics/icon-512.png (source: graphics/src/icon.svg)
- Feature graphic 1024x500: graphics/feature-graphic-1024x500.png (source: graphics/src/feature-graphic.svg)
- Phone screenshots (1080x2160, within Play's 2:1 limit): see screenshots/. Play needs 2 to 8.
  - 01 Calculate sheet, 03 dark theme, 04 calculator, 05 history are ready.
  - 02 needs a RETAKE: it shows the toolbar but not Calculate in the overflow menu.
  - 05 shows test rows under Yesterday: clear History and add realistic sums, then retake.
- Tablet screenshots: skip for first release (wide layout not built yet)
- The launcher icon inside the app is still the Android Studio template. Replace it with this design before building the release AAB.

## App content (policy) answers
| Form | Answer |
|---|---|
| Privacy policy URL | https://vickypoojari.github.io/app-policies/hisaab/privacy/ (after Pages is enabled) |
| Ads | No ads |
| App access | All functionality available without login |
| Content rating (IARC) | Category: Utility. Answer No to violence, sexuality, language, drugs, gambling, user-generated content, location sharing, purchases. Expect Everyone |
| Target audience | 18 and over (keeps you out of the Families policy) |
| News app / Government app / Health | No / No / No |
| Financial features | "My app doesn't provide any financial features" (it is a calculator, no money moves) |
| Data safety: collect or share data | No. Does not collect or share any user data |
| Data safety: encryption in transit | Not applicable, app sends nothing |
| Data safety: deletion request | Not applicable (no account, data stays on device) |
| Permissions | None declared. Verify on the built AAB (see below) |

History is stored only on the phone and is excluded from cloud backup, so "no data collected" is accurate.

## Verify before you submit
```bash
./gradlew bundleRelease
# merged manifest must show no INTERNET or other uses-permission
unzip -p app/build/outputs/bundle/release/app-release.aab base/manifest/AndroidManifest.xml | strings | grep -i permission
```
If any library adds INTERNET, the "no internet" claim and Data safety answer must change.
