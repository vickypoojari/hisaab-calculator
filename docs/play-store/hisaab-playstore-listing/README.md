# Hisaab - Calculator: Play Store listing pack

Status: DRAFT. A human must review it before upload. Created 2026-11-21.

## Contents
- listing.md: name, short and full description, category, policy form answers, pre-submit check
- release-notes.md: "What's new" text for 1.0.0
- policy/privacy-policy.md: host as a public page and paste its URL into Play Console. Add your contact email first.
- graphics/icon-512.png: Play icon 512x512
- graphics/feature-graphic-1024x500.png: feature graphic
- graphics/src/: SVG sources
- screenshots/phone/: phone screenshots 1080x2160

## Before you upload
1. Retake two screenshots (files marked RETAKE): 02 must show Calculate in the open overflow menu (scroll the menu); 05 should have realistic sums, not test rows.
3. Replace the in-app launcher icon (still the Android Studio template) with the new icon design, then build the signed AAB.
4. Confirm the built AAB has no INTERNET permission (command in listing.md). If it does, the "no internet" claims and Data safety answer must change.
5. Check "Hisaab - Calculator" is free when you create the app in Play Console.

## Upload order in Play Console
Create app, then Store listing (text, icon, feature graphic, screenshots), App content (privacy policy URL, ads, content rating, target audience, Data safety, financial features), then Testing track, then Production.
