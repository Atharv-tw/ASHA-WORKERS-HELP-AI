# ASHA Worker Voice AI — AI Field Companion for Rural Healthcare

## Overview

ASHA (Accredited Social Health Activist) workers are the backbone of India's rural
healthcare system. Each worker typically manages the health records of around 150–200
families — today mostly on paper registers and from memory, which leads to incomplete
records, missed immunizations, and delayed referrals.

This project is an **offline, voice-first Android app** that acts as a **memory +
planning companion** for ASHA workers. It remembers every household's health history and
proactively guides the worker on what to do next. The core principle: **the AI reduces
the thinking burden, not just the data-entry burden.**

Everything runs **on-device and offline** — suitable for the most remote villages — and
is **voice-first in Hindi (Devanagari)** so it works without typing or reading complex forms.

---

## The three pillars

1. **Memory Engine** — recalls a household's history on demand.
   *"राजू का घर"* → *"पिछली विज़िट में बुखार थी… टीकाकरण बाकी है।"*
2. **Daily Visit Planner** — a spoken morning briefing of the day's priority visits, plus
   a daily notification.
3. **Conversational Guidance & Risk Alerts** — conservative, guideline-based decision
   support (IMNCI/HBNC danger signs) with referral prompts. *Decision support, not diagnosis.*

---

## Architecture

```
ui/        MainActivity (voice) · TodayPlanActivity · PatientListActivity
domain/    IntentRouter · HealthDataParser · ClinicalEngine (+ClinicalRules)
           SymptomLabels · MemoryNarrator
           text/DevanagariNormalizer · text/PhoneticKey
           schedule/ImmunizationScheduler · schedule/PregnancyTracker
           planner/BriefingGenerator
data/      Room (Household · Patient · Visit · Immunization · FollowUpTask)
           AshaRepository (Dispatchers.IO) · DbKey (SQLCipher key)
platform/  VoskService (offline STT) · TtsService (Hindi TTS)
           work/MorningBriefingWorker (WorkManager)
assets/    model-hi/ (Vosk Hindi model, Git LFS) · rules.json · immunization_schedule.json
```

**Voice loop:** microphone → Vosk (offline STT, Devanagari) → `DevanagariNormalizer` →
`IntentRouter` → parse / recall / guidance / plan → Room (encrypted) → Hindi TTS reply.

All spoken input flows through deterministic, unit-tested keyword/regex logic — no network,
no cloud model — so it is fully offline and reproducible.

## Technology stack

- **Language / build:** Kotlin, Gradle 8.7, AGP 8.3.2, JDK 21
- **Speech-to-text:** [Vosk](https://alphacephei.com/vosk/) small Hindi model, bundled in
  `assets/model-hi` via **Git LFS**
- **Text-to-speech:** Android `TextToSpeech` (hi-IN)
- **Database:** Room, encrypted at rest with **SQLCipher** (key in EncryptedSharedPreferences)
- **Background:** WorkManager (daily briefing notification)
- **minSdk 24, targetSdk 34**

## Build & run

Prerequisites: Android SDK (platform 34, build-tools 35), a JDK 21 (Android Studio's
bundled JBR works), and **Git LFS** (`git lfs install`) to fetch the voice model.

```bash
git lfs install
git clone <repo> && cd AshaWorkerAi
./gradlew assembleDebug        # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install onto a running emulator/device
./gradlew testDebugUnitTest    # run the unit test suite
```

`local.properties` must point at your SDK (`sdk.dir=...`). The build is pinned to JDK 21 via
`gradle.properties` (`org.gradle.java.home`); adjust that path for your machine or set
`JAVA_HOME`.

> **Note:** the app requires a Hindi TTS voice on the device and microphone permission.
> First launch unpacks the ~79 MB model, so give it a few seconds to reach
> *"रिकॉर्ड के लिए तैयार"*.

## Testing

The logic-heavy layers are covered by JVM unit tests (`./gradlew testDebugUnitTest`):
Devanagari parsing, intent routing, clinical rule evaluation + rules-JSON loader, phonetic
name matching, memory narration, immunization/pregnancy date math, and briefing generation.
Room's DAO queries are validated at compile time by the Room annotation processor.

## Privacy & security

- **Offline-only:** the app declares **no INTERNET permission**; nothing leaves the device.
- **Encrypted at rest:** the SQLite database is encrypted with SQLCipher using a random
  256-bit key stored in Android Keystore-backed EncryptedSharedPreferences.
- **No cloud backup of PII:** `allowBackup="false"`.

## Disclaimer

This app provides **guideline-based decision support, not a medical diagnosis**. Guidance is
conservative and referral-biased. Clinical content should be reviewed by a qualified advisor
before any real-world deployment.
