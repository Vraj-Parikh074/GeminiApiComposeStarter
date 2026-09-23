# 🤖 Gemini AI Assistant - Android App

A feature-rich, production-ready Android application built with **Jetpack Compose**, **Room SQLite**, and the **Google Gemini AI API**.

![Android](https://img.shields.com/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.com/badge/Language-Kotlin-purple.svg)
![Compose](https://img.shields.com/badge/UI-Jetpack%20Compose-blue.svg)
![Gemini](https://img.shields.com/badge/AI-Google%20Gemini-orange.svg)

---

## ✨ Features

- 🤖 **Multi-Persona AI Roles**: Switch between **Standard AI**, **Code Architect**, **Creative Writer**, **Academic Tutor**, and **ELI5 Explainer**, or create custom AI personas!
- 🎨 **Markdown & Code Block Syntax Renderer**: Beautiful dark theme code block cards with uppercase language badges and one-tap **Copy Code** buttons.
- 🗣️ **Native Text-To-Speech (TTS)**: Tap the speaker icon on any response to hear Gemini read answers out loud.
- 🎙️ **Speech-To-Text (STT)**: Dictate prompts hands-free using Android's native Voice Input.
- 💡 **Smart Quick Chips**: One-tap suggestion chips for fast prompt generation.
- 🔄 **Retry & Regenerate**: Easily retry previous prompts or share responses via Android Share Sheet.
- 🌡️ **AI Creativity Control**: Adjustable temperature slider (0.0 Factual to 1.0 Creative).
- 📲 **External Text Sharing**: Share text from Chrome, WhatsApp, or any app directly into Gemini AI Assistant (`ACTION_SEND`).
- 🔒 **Encrypted Key Storage**: API key security using `EncryptedSharedPreferences` and `local.properties`.
- 💾 **Room Database**: Persistent local chat history stored with Room SQLite.

---

## 🚀 Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Vraj-Parikh074/GeminiApiComposeStarter.git
   ```

2. **Get a Gemini API Key**:
   - Get a free API key from **[Google AI Studio](https://aistudio.google.com/app/apikey)** (key starts with `AIzaSy...`).

3. **Configure `local.properties`**:
   - Open `local.properties` in the project root and add your key:
     ```properties
     GEMINI_API_KEY=AIzaSyYourActualKeyHere
     ```

4. **Build & Run**:
   - Open the project in **Android Studio** and run on an Emulator or Physical Device!

---

## 🛠️ Built With

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
- **Database**: [Room SQLite](https://developer.android.com/training/data-storage/room)
- **AI Engine**: [Google Generative AI SDK (`com.google.ai.client.generativeai`)](https://ai.google.dev/)
- **Security**: [AndroidX Security Crypto (`EncryptedSharedPreferences`)](https://developer.android.com/topic/security/data)
- **Concurrency**: Kotlin Coroutines & `StateFlow`
