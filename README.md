```markdown
# Vistaar Setu (विस्तार सेतु)

**Vistaar Setu** is an AI-powered offline-first educational bridge designed to break language barriers in single-teacher, multi-grade tribal classrooms. It enables educators to convert Hindi lessons into local mother-tongue audio explanations (Santali / Ol Chiki), bilingual flashcards, and automated classroom practice worksheets using a single Android device.

---

## 📁 Repository Structure

```text
vistaar_setu/
├── backend/                  # FastAPI AI Translation & Audio Processing Server
│   ├── api/                  # API Endpoint definitions (routes.py)
│   ├── app/                  # Application core entry points (main.py)
│   ├── schemas/              # Pydantic data schemas (lesson.py)
│   └── services/             # Translation & TTS processing services
├── android_ui/               # Android Native Mobile Application
│   └── android/              # Jetpack Compose UI, ViewModel & Retrofit setup
├── data/                     # Offline local resources & curricula
│   ├── glossary/             # Key educational vocabulary mappings
│   └── sample_curriculum/    # Standard subject-wise lesson plans
├── test_indictrans.py        # Standalone script for translation pipeline testing
├── test_santali_tts.py       # Standalone script for text-to-speech audio synthesis
├── requirements.txt          # Python dependencies for the backend
└── vistaar-setu-app.apk      # Compiled Android application package

```

---

## 🛠️ Tech Stack & Architecture

* **Frontend:** Android (Kotlin, Jetpack Compose, Retrofit, Room Database)
* **Backend Framework:** FastAPI (Python)
* **AI Pipelines:** IndicTrans2 (Translation), Speech Synthesis / TTS (Santali Audio Output)
* **Storage & Caching:** Local SQLite / Room DB (Offline playback and local worksheet rendering)

---

## ⚡ Quick Start Guide

### 1. Backend Setup

```bash
# Clone the repository
git clone [https://github.com/your-username/vistaar_setu.git](https://github.com/your-username/vistaar_setu.git)
cd vistaar_setu

# Create and activate a virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install required packages
pip install -r requirements.txt

# Start the FastAPI server
uvicorn backend.app.main:app --host 0.0.0.0 --port 8000 --reload

```

### 2. Expose Local Server via ngrok (For Android Testing)

```bash
ngrok http 8000

```

Copy the generated `https://xxxx.ngrok-free.app` URL and update the `BASE_URL` in your Android project's network configuration layer.

### 3. Android Application Setup

1. Open the `android_ui/android` folder in **Android Studio**.
2. Sync the project with Gradle files (`build.gradle.kts`).
3. Update `BASE_URL` in `RetrofitClient.kt` with your active backend endpoint.
4. Run the app on an emulator or connect a physical Android device.

---

## 📱 Features

* **One-Device Classroom Broadcast:** Single phone setup designed to project native Santali audio lessons to multi-grade classrooms.
* **Auto-Generated Bilingual Cards:** Side-by-side display of source Hindi text and target Santali text for blackboard reproduction.
* **Automated Practice Worksheets:** Extracts key terminology and creates classroom exercises automatically from lesson speech.
* **Offline Caching:** Saves lesson metadata and synthesized audio locally via Room DB for offline classroom execution.

``` 