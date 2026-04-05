# ASHA Worker Voice AI – AI Field Companion for Rural Healthcare

## Overview

ASHA (Accredited Social Health Activist) workers are the backbone of India's rural healthcare system. Each worker typically manages the health records of around 150–200 families. Currently, most of this information is maintained in paper registers and through memory, which leads to incomplete records, missed immunizations, and delayed referrals.

This project proposes an **offline AI field assistant** that acts as a **memory + planning companion** for ASHA workers. It remembers every household’s health history and proactively guides workers on what to do next.

The core principle of the system is that **the AI reduces the thinking burden**, not just the data entry burden.

---

# Problem Statement

ASHA workers are responsible for tracking pregnancies, vaccinations, illnesses, and follow-ups across hundreds of households. However, they rely heavily on paper registers and personal memory to manage this information.

Existing digital health applications often fail because they focus on **collecting and reporting data** for supervisors, rather than **guiding and remembering** for the worker. This results in:

- High cognitive load (remembering who needs what)
- Fragmented health records
- Missed immunization schedules
- Delayed medical referrals

There is a critical need for a system that enables **frictionless health management without requiring literacy or typing**.

---

# Proposed Solution: AI Field Companion

We propose a **voice-first offline AI assistant** that functions as a digital memory and decision-support system. Instead of just "recording data," the system focuses on three core abilities:

### 1. Memory Engine
The system remembers past visits and brings them up automatically. It acts as the worker's long-term memory.
- **Example:** ASHA says: *"Raju ka ghar"*
- **System responds:** *"Last visit: diarrhea case. Follow-up needed. Also, his 2nd polio dose is pending."*

### 3. Daily Visit Planner
The AI acts as a personal secretary, providing a morning briefing to prioritize work.
- **Example:** *"Today's priority visits: 1. Meena (Vaccine due), 2. Aman (Fever follow-up), 3. Pooja (Pregnancy week 32 check)."*

### 3. Conversational Guidance & Risk Alerts
The system provides real-time decision support based on national health guidelines.
- **Example (ASHA asks):** *"3 din se bukhar ho toh kya karein?"*
- **AI replies:** *"Check for shivering. If child age < 5 and fever > 2 days, possible malaria risk. Suggest PHC referral immediately."*

Key design principles:

- **Voice-first:** No typing or reading complex forms.
- **Offline-first:** Works in the most remote villages.
- **Memory-native:** Reduces the cognitive burden of tracking 200 families.
- **Proactive:** Tells the worker what to do, rather than just waiting for input.

---

# Technical Architecture

The system follows an **offline-first intelligence architecture**.

ASHA Worker Voice Input
↓
Offline Speech Recognition
↓
Natural Language Parsing (Intent & Data)
↓
**Memory & Planning Engine** (Context Retrieval)
↓
**Health Rule Engine** (Decision Support)
↓
Voice Response (Text-to-Speech)


---

# Technology Stack

## Mobile Application
- **Framework:** Native Android (Kotlin)
- **Database:** SQLite (Room) for local persistent memory.
- **Speech-to-Text:** Vosk (Offline)
- **Text-to-Speech:** Android TTS API

---

# Natural Language Parsing

The spoken sentence must be converted into structured medical data and intent.

Example:
Input: *"Sunita ka beta 3 saal bukhar 3 din se"*
Parsed Output:
```json
{
  "intent": "record_visit",
  "patient": "Sunita ka beta",
  "age": 3,
  "symptom": "fever",
  "duration": "3 days"
}
```

---

# Local Health Database

All patient data is stored locally using **SQLite**. The database acts as the "Memory Engine," storing entire household histories to provide context for future visits.

---

# Clinical Decision Engine

The system identifies high-risk cases automatically using national health guidelines.
- **Example:** Child fever > 3 days → Suggest PHC referral.

---

# Impact and Benefits

- **Reduced Cognitive Load:** ASHA workers no longer need to remember hundreds of health records.
- **Proactive Care:** System identifies follow-ups and missed vaccinations automatically.
- **Offline Reliability:** No dependency on internet for core field work.
- **Early Medical Referrals:** AI identifies risk conditions before they become critical.

---

# Potential Future Features

### Pregnancy Monitoring
Automatic reminders for antenatal visits based on LMP.

### Child Growth Tracking
Weight and nutrition monitoring with visual alerts.

### Doctor Teleconsult Integration
Voice-based referral to remote doctors when internet is available.

---

# Conclusion

The proposed system transforms how ASHA workers interact with healthcare data by replacing paper registers with a **proactive AI Field Companion**. By prioritizing **memory and guidance**, the solution empowers frontline health workers to deliver better care with less effort.
