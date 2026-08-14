# HealthLogs: Health Logs Tracker

A modern, focused Android application designed to streamline your fitness and nutrition tracking. Built with Kotlin and Jetpack Compose, HealthLogs offers a clean, card-based interface for logging workouts and meals while keeping your data local and secure.

---

## Table of Contents
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Workout Logging**: Track exercises, sets, weight, and reps with ease.
- **Nutrition Tracking**: Log meals with calories and optional macros (protein, carbs, fat).
- **BYOK AI Integration**: "Bring Your Own Key" AI support for OpenAI, Gemini, Groq, and local endpoints (Ollama/LM Studio). Use AI to estimate meal calories or workout burn.
- **AI Transparency**: View the exact prompt and logical assumptions the AI used for every estimation through the **AI Details** bottom sheet.
- **Smart Weight Tracking**: Log your weight daily with a "one entry per day" constraint. Auto-populates from your last entry to reduce friction.
- **Weight Trends**: Tasteful trend charts with X and Y axis labels to visualize your progress over time.
- **Personalized Profiles**: Set your height (cm or ft/in) and gender to provide the AI with more context for accurate physiological estimations.
- **Data Sovereignty**: Import/Export your entire database via JSON files. Your data stays with you.
- **Secure Storage**: API keys are stored securely on-device using the Android Keystore system.
- **Adaptive UI**: Full support for Light and Dark modes using Material 3.
- **Date Navigation**: Use the built-in calendar to review or log data for any day in your fitness journey.

---

## Screenshots

### Light Mode
| Dashboard | Workout Editor | Meal Editor | Daily Workouts | Daily Meals | Settings |
| :---: | :---: | :---: | :---: | :---: | :---: |
| <img src="app/src/main/app/src/main/assets/screenshots/1.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/2.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/3.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/4.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/5.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/6.jpg" width="220" /> |

### Dark Mode
| Dashboard | Workout Editor | Meal Editor | Daily Workouts | Daily Meals | Settings |
| :---: | :---: | :---: | :---: | :---: | :---: |
| <img src="app/src/main/app/src/main/assets/screenshots/1_dark.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/2_dark.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/3_dark.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/4_dark.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/5_dark.jpg" width="220" /> | <img src="app/src/main/app/src/main/assets/screenshots/6_dark.jpg" width="220" /> |

---

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Security**: [EncryptedSharedPreferences](https://developer.android.com/topic/security/data) & Android Keystore
- **JSON Serialization**: [Moshi](https://github.com/square/moshi)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & OkHttp
- **Dependency Injection**: Manual injection via `ViewModelFactory`
- **Asynchronous Work**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & Flow
- **Testing**:
    - [Roborazzi](https://github.com/takahirom/roborazzi) for screenshot testing.
    - JUnit 4 & Robolectric.
- **Build Utilities**: 
    - [KSP](https://kotlinlang.org/docs/ksp-overview.html) (Kotlin Symbol Processing).
    - [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) for `.env` management.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer.
- JDK 17 (recommended for Gradle toolchain).

### Setup
1. **Clone the repo**:
   ```bash
   git clone https://github.com/Adwait-P-Mishra/Gym-Logs-android
   ```
2. **Environment Variables**:
   Create a `.env` file in the project root. This project uses the `Secrets Gradle Plugin` to securely handle local properties.
   ```text
   # .env
   # Add any required secrets here
   ```
3. **Build & Run**:
   Sync Gradle in Android Studio and run the `app` module on your emulator or physical device.

---

## Project Structure

The project follows a feature-oriented package structure within `com.adprmi.healthLogs`:

- `ui/`: Contains Compose screens (`screens/`), reusable components (`components/`), and themes.
- `data/`: Room entities, DAOs, and repositories for data orchestration.
- `viewmodel/`: State holders for UI components using `ViewModel` and `Flow`.
- `model/`: Domain data models and data transfer objects (DTOs).
- `util/`: Helper classes for dates and formatting.

---

## License

Distributed under the MIT License. See `LICENSE` for more information.

---
