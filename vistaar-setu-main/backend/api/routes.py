from fastapi import APIRouter

from schemas.lesson import (
    TranslationRequest,
    TranslationResponse,
    LessonProcessRequest,
    LessonProcessResponse
)

from services.translation import TranslationService
from services.lesson_processor import LessonProcessor


router = APIRouter()

translation_service = TranslationService()
lesson_processor = LessonProcessor()


@router.post(
    "/translate",
    response_model=TranslationResponse
)
def translate_lesson(request: TranslationRequest):

    language_map = {
        "hi": "hin_Deva",
        "sat": "sat_Olck"
    }

    source_language = "hin_Deva"

    target_language = language_map.get(
        request.target_language,
        request.target_language
    )

    translated_text = translation_service.translate(
        text=request.text,
        source_language=source_language,
        target_language=target_language
    )

    return TranslationResponse(
        source_language="hi",
        target_language=request.target_language,
        translated_text=translated_text
    )


@router.post(
    "/lesson/process",
    response_model=LessonProcessResponse
)
def process_lesson(request: LessonProcessRequest):

    result = lesson_processor.process(
        text=request.text,
        target_language=request.target_language
    )

    return LessonProcessResponse(
        source_text=result["source_text"],
        target_language=result["target_language"],
        translated_text=result["translated_text"],
        audio_file=result["audio_file"],
        status=result["status"]
    )