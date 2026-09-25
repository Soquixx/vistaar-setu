from services.translation import TranslationService
from services.tts import TTSService
from services.activity_generator import ActivityGenerator


class LessonProcessor:

    def __init__(
        self,
        translation_service: TranslationService = None
    ):
        # Reuse the already-created translation service.
        self.translation_service = (
            translation_service
            or TranslationService()
        )

        # TTS model is loaded ONCE here.
        self.tts_service = TTSService()

        # No AI model is loaded by ActivityGenerator.
        self.activity_generator = ActivityGenerator()

    def process(
        self,
        text: str,
        target_language: str,
        grade: int,
        learning_area: str,
        learning_objective: str
    ):

        text = text.strip()
        target_language = target_language.strip()
        learning_area = learning_area.strip()
        learning_objective = learning_objective.strip()

        if not text:
            raise ValueError(
                "Text cannot be empty."
            )

        if not learning_objective:
            raise ValueError(
                "Learning objective cannot be empty."
            )

        language_map = {
            "hi": "hin_Deva",

            "sat": "sat_Olck",
            "Santali": "sat_Olck",
            "santali": "sat_Olck",
            "sat_Olck": "sat_Olck"
        }

        target_code = language_map.get(
            target_language,
            target_language
        )

        # --------------------------------------------------
        # 1. TRANSLATION
        # --------------------------------------------------

        translated_text = self.translation_service.translate(
            text=text,
            source_language="hin_Deva",
            target_language=target_code
        )

        translated_text = translated_text.strip()

        if not translated_text:
            raise RuntimeError(
                "Translation service returned empty text."
            )

        # --------------------------------------------------
        # 2. TEXT TO SPEECH
        # --------------------------------------------------

        audio_file = self.tts_service.generate_audio(
            translated_text
        )

        filename = (
            audio_file
            .replace("\\", "/")
            .split("/")[-1]
        )

        audio_url = f"/audio/{filename}"

        # --------------------------------------------------
        # 3. FLASHCARDS
        # --------------------------------------------------

        flashcards = (
            self.activity_generator.generate_flashcards(
                source_text=text,
                translated_text=translated_text
            )
        )

        # --------------------------------------------------
        # 4. WORKSHEET
        # --------------------------------------------------

        worksheet = (
            self.activity_generator.generate_worksheet(
                source_text=text,
                translated_text=translated_text,
                grade=grade,
                learning_area=learning_area,
                learning_objective=learning_objective
            )
        )

        # --------------------------------------------------
        # 5. FINAL LESSON RESPONSE
        # --------------------------------------------------

        return {
            "source_text": text,

            "target_language": target_language,

            "translated_text": translated_text,

            "audio_url": audio_url,

            "audio_filename": filename,

            "title": self._generate_title(
                text=text,
                learning_area=learning_area
            ),

            "grade": grade,

            "learning_area": learning_area,

            "learning_objective": learning_objective,

            "verification_status": "development",

            "flashcards": flashcards,

            "worksheet": worksheet,

            "status": "success"
        }

    @staticmethod
    def _generate_title(
        text: str,
        learning_area: str
    ) -> str:

        cleaned = " ".join(
            text.strip().split()
        )

        if not cleaned:
            return learning_area

        words = cleaned.split()

        if len(words) > 8:
            cleaned = (
                " ".join(words[:8])
                + "..."
            )

        return cleaned