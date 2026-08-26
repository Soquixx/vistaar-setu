import os
import uuid

import torch
import soundfile as sf
from transformers import AutoTokenizer
from parler_tts import ParlerTTSForConditionalGeneration


MODEL_NAME = "ai4bharat/indic-parler-tts"


class TTSService:

    def __init__(self):
        """
        Load the TTS model ONCE when the service is created.
        """

        self.device = "cuda" if torch.cuda.is_available() else "cpu"

        print(f"TTSService device: {self.device}")
        print("Loading Indic Parler-TTS tokenizer...")

        self.tokenizer = AutoTokenizer.from_pretrained(
            MODEL_NAME
        )

        print("Loading Indic Parler-TTS model...")

        self.model = ParlerTTSForConditionalGeneration.from_pretrained(
            MODEL_NAME
        ).to(self.device)

        self.model.eval()

        print("Indic Parler-TTS ready.")

        # Directory where generated audio files will be stored
        self.audio_dir = os.path.join(
            "generated_audio"
        )

        os.makedirs(
            self.audio_dir,
            exist_ok=True
        )

    def generate_audio(
        self,
        text: str
    ) -> str:

        """
        Convert Santali text into an audio file.

        text:
            Santali text produced by TranslationService.

        returns:
            Path of generated WAV file.
        """

        if not text or not text.strip():
            raise ValueError(
                "Text cannot be empty."
            )

        # Speaker/style description
        description = (
            "Arjun speaks naturally in Santali with a clear, "
            "friendly teaching voice. The recording is very high "
            "quality with no background noise."
        )

        # Tokenize description
        description_input_ids = self.tokenizer(
            description,
            return_tensors="pt"
        ).input_ids.to(self.device)

        # Tokenize dynamically supplied Santali text
        prompt_input_ids = self.tokenizer(
            text,
            return_tensors="pt"
        ).input_ids.to(self.device)

        # Generate audio
        with torch.no_grad():

            generation = self.model.generate(
                input_ids=description_input_ids,
                prompt_input_ids=prompt_input_ids
            )

        # Convert generated tensor to NumPy
        audio_arr = (
            generation
            .cpu()
            .numpy()
            .squeeze()
        )

        # Unique filename
        filename = (
            f"santali_{uuid.uuid4().hex}.wav"
        )

        output_file = os.path.join(
            self.audio_dir,
            filename
        )

        # Save WAV
        sf.write(
            output_file,
            audio_arr,
            self.model.config.sampling_rate
        )

        return output_file