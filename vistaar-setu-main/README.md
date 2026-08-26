# Vistaar Setu AI Backend

AI-powered backend for converting Hindi educational content into Santali text and speech.

## Overview

Vistaar Setu processes educational content through a Hindi → Santali → Speech pipeline.

### Pipeline

Hindi Text
↓
IndicTrans2
↓
Santali Text
↓
Indic Parler-TTS
↓
Santali Audio (WAV)

## Features

- Hindi → Santali translation
- Santali text-to-speech generation
- REST API using FastAPI
- Swagger API testing
- Generated audio file serving
- Input validation
- GPU acceleration with CUDA when available
- Model loading once during application startup

## Technology Stack

- Python 3.10
- FastAPI
- Uvicorn
- PyTorch
- Hugging Face Transformers
- IndicTransToolkit
- IndicTrans2
- Indic Parler-TTS
- Pydantic
- SoundFile
- CUDA / NVIDIA GPU support

## Project Structure

```text
vistaar-setu/
│
├── backend/
│   ├── api/
│   │   ├── __init__.py
│   │   └── routes.py
│   │
│   ├── app/
│   │   ├── __init__.py
│   │   └── main.py
│   │
│   ├── schemas/
│   │   ├── __init__.py
│   │   └── lesson.py
│   │
│   └── services/
│       ├── __init__.py
│       ├── lesson_processor.py
│       ├── translation.py
│       └── tts.py
│
├── test_indictrans.py
├── test_santali_tts.py
├── requirements.txt
├── .gitignore
└── README.md