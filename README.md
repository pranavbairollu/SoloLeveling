# 🌌 Solo Leveling: The System

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![Aesthetic](https://img.shields.io/badge/Aesthetic-Monarch-magenta.svg)](#-monarch-mode--authority)

> **"Congratulations. You have become a Player."**

Solo Leveling: The System is a hyper-immersive, gamified productivity engine for Android. It transforms your real-world effort into a high-stakes RPG experience, enforcing discipline through the "System's" absolute authority. This is not just a habit tracker; it is your path to becoming a Monarch.

---

## ⚡ The Core Directives

The System tracks every action. Success brings growth, XP, and rank advancement (from **E-Rank** to **S-Rank** and beyond). Failure is not ignored—the System enforces penalties for those who neglect their Quests or retreat from Gates.

### 🎭 1. The Awakening & The Contract
Your journey begins with **The Awakening**. 
- **Role Selection:** Choose your class—*Student, Athlete, Executive,* or *Custom*—each with unique starting stat modifiers.
- **The Great Contract:** You must accept the System's terms. Once signed, the System's commands are absolute.
- **System Boot:** A high-intensity initialization sequence prepares your "Player" status.

### 📊 2. Player Stats & Progression Engine
A robust, type-safe stat system tracks your evolution across:
- **Fitness (STR):** Physical activity and health.
- **Knowledge (INT):** Learning and mental growth.
- **Discipline (VIT):** Consistency and routine.
- **Awareness (AGI):** Focus and reaction time.
- **Charisma (CHA):** Social and leadership milestones.
- **Luck (LCK):** The hidden factor in rewards and gate events.

### ⚔️ 3. The Gates (Strict Focus Sessions)
Gates are high-stakes focus sessions managed by the `GateFocusService`.
- **System Does Not Permit Retreat:** Once you enter a Gate, leaving the app triggers an immediate "Gate Collapse."
- **Red Gates:** There is a 15% chance a Gate will turn into a **Red Gate**, doubling both rewards and the risk of penalty.
- **Immersive Feedback:** Real-time haptics and screen-shake effects simulate the intensity of the dungeon.

### 🐲 4. Boss Raids & Shadow Extraction
Major real-life goals are categorized as **Boss Raids**.
- **Qualification Checks:** You cannot engage a Boss without meeting specific Level and Stat requirements.
- **Permanent Logging:** Every victory or defeat is etched into the System's history.
- **"ARISE":** Upon defeating a Boss, use your actual voice to command **"ARISE"**. The `SpeechRecognizer` API validates your authority, allowing you to extract the Boss's shadow for permanent passive multipliers.

### 👑 5. Monarch Mode & Authority
The ultimate endgame state for those who reach the pinnacle.
- **Monarch Authority:** Absolute immunity to standard penalties.
- **System Multiplier:** A 2.0x stat growth multiplier across all activities.
- **Shadow Loyalty:** Locked shadow persistence and enhanced visual "Gold/Magenta" aesthetics.
- **System Obedience:** A total UI overhaul reflecting your status as the Monarch.

---

## 🎨 System UX & Immersion
The System is designed to be felt, not just seen:
- **Level Up Ceremony:** Dynamic particle effects and screen-shake for every rank increase.
- **Glitch Effects:** Visual "System Errors" and glitch animations during high-stress resets or penalty triggers.
- **Haptic Feedback:** Tactile responses for critical system events.
- **Deep Immersion:** Navigation is blocked during high-stakes activities to ensure absolute focus.

---

## 🛡️ Security & System Integrity

The "System" is protected by production-grade security protocols to prevent unauthorized data manipulation and ensure that your growth is authentic.

- **Absolute Data Encryption:** The core database is encrypted via **SQLCipher (AES-256)**. Stats, levels, and shadow data are unreadable by external tools.
- **Secure Persistence:** Sensitive user settings and class data are stored in hardware-backed **EncryptedSharedPreferences**.
- **Extraction Prevention:** ADB backups are disabled to prevent unauthorized data cloning or side-loading stat modifications.
- **System Integrity Checks:** The System monitors for compromised environments. If root access or emulator simulation is detected, the System logs a "Critical Integrity Compromised" warning.
- **Code Hardening:** The codebase is obfuscated via **R8/ProGuard**, protecting the "System's" inner workings from reverse engineering.

---

## 🛠️ Technical Architecture
- **Language:** 100% Kotlin
- **Framework:** MVVM with Clean Architecture principles.
- **State Management:** Background-resilient focus tracking using `ForegroundServices`.
- **Persistence:** Atomic updates via Room Database, ensuring no progress is lost.
- **Reliability:** `WorkManager` integration for precision midnight system resets and penalty calculations.
- **Interactions:** Android `SpeechRecognizer` for voice-activated commands.

---

## 🚀 Initialization
To install the System on your device:

1. **Clone the Source:**
   ```bash
   git clone https://github.com/pranavbairollu/SoloLeveling.git
   ```
2. **Launch in Android Studio:**
   - Open the project directory.
   - Wait for the Gradle sync (Syncing the System's logic).
3. **Deploy:**
   - Run on a physical device for full Haptic and Voice recognition support.
   - Grant **Microphone** and **Notification** permissions when prompted by the System.

---

## 📜 System Disclaimer
*This is an unofficial fan project inspired by "Solo Leveling". It is a productivity tool and is not affiliated with the official franchise owners.*

---
<p align="center">
  <b>"The System will use you, and you will use the System."</b><br>
  <i>Will you remain a weak hunter, or will you level up?</i>
</p>
