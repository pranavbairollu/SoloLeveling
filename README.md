# Solo Leveling - System

A powerful, gamified productivity and lifestyle tracker for Android, inspired by the anime and manhwa *Solo Leveling*. Turn your daily tasks, focus sessions, and big goals into a high-stakes RPG experience.

## 🌟 The Core Concept

Awaken as a Player. Through the **System**, everything you do in real life rewards you with XP, ranks, and stat boosts. But be warned: the System is strict. Failure to keep up with daily quests or safely conquer gates results in penalties—and potentially losing your hard-earned endurance (HP).  

## 🔥 Features & Mechanics

### 1. The Awakening & Contract
Upon first launching the system, you undergo **The Awakening**. Choose your starting Class—**Student**, **Athlete**, **Executive**, or **Custom**—which defines your initial paths and stat priorities. Before proceeding, you must sign The Contract and agree to follow the System's commands.

### 2. Player Stats & Progression
Your growth is tracked across a complex RPG system, progressing from **E-Rank** up to **S-Rank** and beyond:
- **Fitness** (Strength)
- **Knowledge** (Intelligence)
- **Discipline** (Vitality)
- **Awareness** (Agility)
- **Charisma** 
- **Luck**
Manage your **Endurance** (your HP) carefully; failure brings you closer to zero.

### 3. Quests (Daily Routines)
The System assigns you daily routines formatted as Quests. Each Quest maps to a specific stat. 
- Complete them to earn stat points, XP, and rank up.
- Ignore them, and the System puts you into a Penalty State, affecting your growth and punishing you! 

### 4. Gates (Strict Focus Sessions)
Gates represent dedicated focus sessions. You must "Survive" for the required time limit.
- **Red Gates Detection:** Randomly, ordinary Gates upgrade to perilous Red Gates.
- **Retreat is Impossible:** The app enforces strict screen tracking. If you leave the app while inside a Gate, you instantly fail. If the user yields or loses focus, "Gate Collapsed" is triggered and the rewards are lost.

### 5. Boss Raids (Major Goals)
Bosses represent massive real-life milestones. Defeating them isn't easy; you need to meet the **Required Level** and **Stat-Checks** to win before the Due Date. Completing a boss raid gives you massive XP rewards and a chance to harvest their soul.

### 6. Shadow Extraction (Voice Command)
Once you defeat a Boss, you can extract its Shadow to serve you. 
The System features real **Voice Recognition**: You actually have to speak the legendary command **"ARISE"** into your microphone to succeed. Unlocked Shadows provide you with permanent passive stat multipliers and boosts.

### 7. Monarch Mode & Authority
Endgame progression system. Clear enough Gates and scale enough Bosses, and slowly transition into the role of a Monarch.

## 🚀 Tech Stack
- **Languages:** Kotlin
- **Architecture:** MVVM, Clean Architecture
- **Database:** Room Database (Offline-First capability)
- **UI:** Android XML / Material Design Components
- **APIs:** Android SpeechRecognizer API (for Voice Commands)
- **Platform:** Android 

## 🛠️ Installation & Setup
To run this project locally, simply clone the repository and open it in Android Studio:
1. **Clone the repository:**
   ```bash
   git clone https://github.com/pranavbairollu/SoloLeveling.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio and click on `Open`.
   - Select the `SoloLeveling` folder.
3. **Sync & Build:**
   - Wait for Gradle to finish syncing the dependencies.
   - Build the project and run it on an Android Emulator or a physical device.

**Note:** Ensure that in-app permissions (such as Microphone for Shadow Extraction) are granted when prompted!

## 📜 Disclaimer
This project is an unofficial fan-made productivity tool heavily inspired by the "Solo Leveling" series. It is not affiliated with, endorsed by, or sponsored by the original creators, publishers, or copyright holders of Solo Leveling.

---
*“There is no turning back. From now on, you will level up.”*
