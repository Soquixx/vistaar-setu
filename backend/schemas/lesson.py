from typing import List

from pydantic import BaseModel, Field, field_validator


# --------------------------------------------------
# Translation
# --------------------------------------------------

class TranslationRequest(BaseModel):
    text: str = Field(..., min_length=1)
    target_language: str = Field(..., min_length=1)

    @field_validator("text", "target_language")
    @classmethod
    def validate_required_text(cls, value: str) -> str:
        value = value.strip()

        if not value:
            raise ValueError("Field cannot be empty.")

        return value


class TranslationResponse(BaseModel):
    source_language: str
    target_language: str
    translated_text: str


# --------------------------------------------------
# Flashcards
# --------------------------------------------------

class Flashcard(BaseModel):
    front: str
    back: str
    language: str


# --------------------------------------------------
# Worksheet
# --------------------------------------------------

class WorksheetQuestion(BaseModel):
    question_number: int
    question_type: str
    question: str
    options: List[str] = Field(default_factory=list)
    correct_answer: str
    points: int = 1


# --------------------------------------------------
# Lesson Processing Request
# --------------------------------------------------

class LessonProcessRequest(BaseModel):
    grade: int = Field(..., ge=1)

    learning_area: str = Field(
        ...,
        min_length=1
    )

    learning_objective: str = Field(
        ...,
        min_length=1
    )

    target_language: str = Field(
        ...,
        min_length=1
    )

    text: str = Field(
        ...,
        min_length=1
    )

    @field_validator(
        "learning_area",
        "learning_objective",
        "target_language",
        "text"
    )
    @classmethod
    def validate_required_text(
        cls,
        value: str
    ) -> str:

        value = value.strip()

        if not value:
            raise ValueError(
                "Field cannot be empty."
            )

        return value


# --------------------------------------------------
# Lesson Processing Response
# --------------------------------------------------

class LessonProcessResponse(BaseModel):
    source_text: str
    target_language: str
    translated_text: str

    audio_url: str
    audio_filename: str

    title: str

    grade: int
    learning_area: str
    learning_objective: str

    verification_status: str

    flashcards: List[Flashcard]
    worksheet: List[WorksheetQuestion]

    status: str