# Vistaar Setu – Learning Materials Module

## Overview

The Learning Materials Module converts lesson data into structured, teacher-ready educational materials. It is designed to integrate with the Vistaar Setu backend and Android application.

## Features

- Topic detection from lesson content
- Learning outcome mapping
- Learning objective generation
- Classroom activity generation
- Assessment prompts
- Structured bilingual worksheets
- Flashcard generation
- Draft/review status for generated materials

## Pipeline

The complete module can be accessed through:

`process_lesson(lesson)`

### Flow

Lesson Data  
↓  
Topic Detection  
↓  
Learning Outcome Mapping  
↓  
Learning Objective + Activity + Assessment  
↓  
Worksheet + Flashcards  
↓  
Integration-Ready Output

## Input Format

```json
{
  "lesson_id": "grade1-fln-counting-001",
  "grade": 1,
  "learning_area": "FLN",
  "target_language": "sat",
  "source_text": "...",
  "translated_text": "...",
  "audio_file": "...",
  "status": "draft"
}
Output

The pipeline generates:

Learning material
Topic and learning outcome
Learning objective
Classroom activity
Assessment prompts
Structured bilingual worksheet
Flashcards
Running the Pipeline Test

From the project root, run:

python learning_materials/test_learning_materials_pipeline.py

Current Status

The module has been successfully tested with:

Counting lessons
Number recognition lessons

The module is ready for integration with the backend translation/audio pipeline and the Android application.