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

        # Extract only the filename
        filename = audio_file.replace("\\", "/").split("/")[-1]

        # Create URL served by FastAPI
        audio_url = f"/audio/{filename}"

        return {
            "source_text": text,
            "target_language": target_language,
            "translated_text": translated_text,
            "audio_url": audio_url,
            "status": "success"
        }