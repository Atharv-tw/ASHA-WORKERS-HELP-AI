# ASHA Worker Voice AI – Memory Prosthetic for Rural Healthcare

## Overview

ASHA (Accredited Social Health Activist) workers are the backbone of India's rural healthcare system. Each worker typically manages the health records of around 150–200 families. Currently, most of this information is maintained in paper registers and through memory, which leads to incomplete records, missed immunizations, and delayed referrals.

This project proposes a **voice-first AI assistant** that acts as a **memory prosthetic** for ASHA workers. The application allows workers to speak patient updates naturally in their local dialect, automatically converting the spoken input into structured health records and providing spoken guidance for next actions.

The core principle of the system is that **the interface itself is the innovation** — removing the need for typing, reading, or complex digital interaction.

---

# Problem Statement

ASHA workers are responsible for tracking pregnancies, vaccinations, illnesses, and follow-ups across hundreds of households. However, they rely heavily on paper registers and personal memory to manage this information.

Existing digital health applications often fail because they assume:

- literacy
- familiarity with smartphones
- ability to type or read complex forms
- continuous internet connectivity

These assumptions make current solutions impractical for many ASHA workers, resulting in:

- fragmented health records
- missed immunization schedules
- delayed medical referrals
- heavy administrative workload

There is a critical need for a system that enables **frictionless health data recording without requiring literacy or typing**.

---

# Proposed Solution

We propose a **voice-first offline mobile application** that functions as a digital memory assistant for ASHA workers.

The worker simply speaks natural sentences such as:

> "Sunita ka beta, 3 saal, bukhar 3 din se"

The system will:

1. Convert speech to text
2. Extract structured health information
3. Update the corresponding family health record
4. Check vaccination schedules and medical rules
5. Speak back recommended next actions

Example response:

> "Bachche ko teen din se bukhar hai. Agar kal tak theek na ho toh PHC le jaaiye. DPT booster bhi due hai."

The entire interaction is **voice-driven**, removing the need for reading or typing.

Key design principles:

- Voice-first interaction
- Offline functionality
- Local dialect support
- Minimal user interface
- Automatic health record generation

---

# Technical Architecture

The system follows an **offline-first architecture** designed for low-connectivity environments.


ASHA Worker Voice Input
↓
Offline Speech Recognition
↓
Natural Language Parsing
↓
Structured Health Data
↓
Local SQLite Health Database
↓
Clinical Rule Engine
↓
Voice Response (Text-to-Speech)


---

# Technology Stack

## Mobile Application

Framework options:

- Flutter
- React Native
- Native Android (Kotlin)

Target platform: **Low-cost Android devices**

---

## Speech Recognition (Offline)

Purpose: Convert spoken input to text.

Possible technologies:

- Whisper.cpp
- Vosk Speech Recognition

Requirements:

- Hindi language support
- Dialect robustness
- Offline operation

Example output:


Sunita ka beta 3 saal bukhar 3 din se


---

## Natural Language Parsing

The spoken sentence must be converted into structured medical data.

Example:

Input:


Sunita ka beta 3 saal bukhar 3 din se


Parsed Output:


{
patient: "Sunita ka beta",
age: 3,
symptom: "fever",
duration: "3 days"
}


Implementation options:

### Option 1 – Rule-Based Parsing (Recommended for MVP)

Example rules:


if "bukhar" → fever
if "khansi" → cough
if "3 din" → duration = 3 days


### Option 2 – Small On-Device LLM

Possible models:

- Phi-3 Mini
- Gemma 2B
- Qwen 2.5 3B

Runtime options:

- llama.cpp
- Ollama (if device allows)

---

# Local Health Database

All patient data is stored locally using **SQLite**.

Reasons for SQLite:

- Fully offline
- Lightweight
- Reliable
- Built into Android
- Handles thousands of records easily

Database file example:


health_records.db


---

## Database Schema (Example)

### Families Table


family_id
household_name
village
asha_worker_id


### Patients Table


patient_id
family_id
name
age
gender


### Visit Records


visit_id
patient_id
date
symptom
duration
notes


Example stored record:


Patient: Aman
Age: 3
Symptom: Fever
Duration: 3 days
Date: 2026-04-05


---

# Clinical Decision Engine

The system contains simple medical rules referencing national health guidelines.

Examples:


If child age < 5 AND fever > 2 days
→ recommend PHC visit

If vaccination due
→ notify ASHA worker


The rule engine can be implemented using:

- Python
- JavaScript
- Embedded rule tables

---

# Voice Response (Text-to-Speech)

After processing the input, the system responds using spoken guidance.

Example:

> "Bachche ko bukhar hai. Agar kal tak theek na ho toh doctor ko dikhaiye."

Possible technologies:

- Piper TTS
- Coqui TTS
- Android Text-to-Speech API

Requirements:

- Hindi speech
- Low-latency
- Offline operation

---

# User Interface Design

The interface must remain **extremely simple**.

Recommended layout:


 Record Visit

📂 Family Records

🔊 Listen Guidance


Typical workflow:

1. ASHA presses **Record**
2. Speaks patient update
3. AI processes the input
4. Health record is updated
5. Voice guidance is spoken

No typing or form filling required.

---

# Dialect Handling

India has significant linguistic variation.

To handle dialect differences, the system uses **symptom synonym mapping**.

Example mapping:


bukhar → fever
jwar → fever
tap → fever
fever → fever


Additional dialect datasets can be expanded over time.

---

# Offline Synchronization

The application operates **fully offline**.

When internet connectivity becomes available:


SQLite Database
↓
Encrypted Upload
↓
District Health Server
↓
Central Health Records


This enables integration with:

- government health databases
- doctor dashboards
- analytics systems

---

# Impact and Benefits

The system provides several benefits for frontline healthcare:

### Reduced Cognitive Load

ASHA workers no longer need to remember hundreds of health records.

### Faster Data Entry

Speaking is significantly faster than writing or typing.

### Improved Continuity of Care

Family health records are automatically maintained.

### Reduced Missed Vaccinations

The system flags due immunizations.

### Early Medical Referrals

Simple clinical rules help identify risk conditions.

At national scale, the system could improve healthcare delivery across **millions of rural households**.

---

# Potential Future Features

### Daily Visit Planner

Each morning the system suggests households to visit:

Example:


Today's Visits:

Meena – vaccination due
Raju – diarrhea follow-up
Pooja – pregnancy week 32 check

### Pregnancy Monitoring

Automatic reminders for antenatal visits.

### Child Growth Tracking

Weight and nutrition monitoring.

### Doctor Teleconsult Integration

Voice-based referral to remote doctors.

---

# Research and References

Key sources informing this project include:

- National Health Mission (NHM) reports on ASHA workforce
- World Health Organization studies on community health workers
- Research on voice interfaces for low-literacy populations
- Mobile health (mHealth) system design literature
- Offline speech recognition technologies such as Whisper and Vosk

These references highlight the need for accessible, low-friction digital tools for last-mile healthcare delivery.

---

# Conclusion

The proposed system transforms how ASHA workers interact with healthcare data by replacing paper registers and memory-based tracking with a **voice-driven AI assistant**.

By prioritizing **usability, offline capability, and natural interaction**, the solution empowers frontline health workers and strengthens the foundation of rural healthcare infrastructure.
