from pydantic import BaseModel, Field


class TranslationRequest(BaseModel):

    text: str

    target_language: str


class TranslationResponse(BaseModel):

    source_language: str

    target_language: str

    translated_text: str


class LessonProcessRequest(BaseModel):

    grade: int

    learning_area: str

    target_language: str

    text: str = Field(..., min_length=1)


class LessonProcessResponse(BaseModel):

    source_text: str

    target_language: str

    translated_text: str

    audio_url: str

    status: str