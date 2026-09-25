import threading

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


# One shared inference lock.
# Prevents simultaneous heavy model inference from
# exhausting CPU/RAM on the development machine.
inference_lock = threading.Lock()


# Load translation service once.
translation_service = TranslationService()


# Reuse the same translation service inside LessonProcessor.
# TTSService is also loaded once inside LessonProcessor.
lesson_processor = LessonProcessor(
    translation_service=translation_service
)


# --------------------------------------------------
# Translation endpoint
# --------------------------------------------------

@router.post(
    "/translate",
    response_model=TranslationResponse
)
def translate_lesson(
    request: TranslationRequest
):

    language_map = {
        "hi": "hin_Deva",
        "sat": "sat_Olck",
        "Santali": "sat_Olck",
        "santali": "sat_Olck",
        "sat_Olck": "sat_Olck"
    }

    source_language = "hin_Deva"

    target_language = language_map.get(
        request.target_language,
        request.target_language
    )

    with inference_lock:

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


# --------------------------------------------------
# Complete lesson endpoint
# --------------------------------------------------

@router.post(
    "/lesson/process",
    response_model=LessonProcessResponse
)
def process_lesson(
    request: LessonProcessRequest
):

    with inference_lock:

        result = lesson_processor.process(
            text=request.text,
            target_language=request.target_language,
            grade=request.grade,
            learning_area=request.learning_area,
            learning_objective=request.learning_objective
        )

    return LessonProcessResponse(
        source_text=result["source_text"],
        target_language=result["target_language"],
        translated_text=result["translated_text"],

        audio_url=result["audio_url"],
        audio_filename=result["audio_filename"],

        title=result["title"],
        grade=result["grade"],
        learning_area=result["learning_area"],
        learning_objective=result["learning_objective"],

        verification_status=result[
            "verification_status"
        ],

        flashcards=result["flashcards"],
        worksheet=result["worksheet"],

        status=result["status"]
    )