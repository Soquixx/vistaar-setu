from services.translation import TranslationService
from services.tts import TTSService


class LessonProcessor:

    def __init__(self):
        self.translation_service = TranslationService()
        self.tts_service = TTSService()

    def process(
        self,
        text: str,
        target_language: str
    ):

        # Hindi → Santali
        translated_text = self.translation_service.translate(
            text=text,
            source_language="hin_Deva",
            target_language="sat_Olck"
        )

        # Santali → Audio
        audio_file = self.tts_service.generate_audio(
            translated_text
        )

        return {
            "source_text": text,
            "target_language": target_language,
            "translated_text": translated_text,
            "audio_file": audio_file,
            "status": "success"
        }