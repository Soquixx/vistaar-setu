import re
import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor


MODEL_NAME = "ai4bharat/indictrans2-indic-indic-dist-320M"


class TranslationService:

    def __init__(self):
        # Device
        self.device = "cuda" if torch.cuda.is_available() else "cpu"

        print(f"TranslationService device: {self.device}")

        # IndicTrans2 processor
        self.processor = IndicProcessor(inference=True)

        # Tokenizer
        self.tokenizer = AutoTokenizer.from_pretrained(
            MODEL_NAME,
            trust_remote_code=True
        )

        # Model
        self.model = AutoModelForSeq2SeqLM.from_pretrained(
            MODEL_NAME,
            trust_remote_code=True
        ).to(self.device)

        self.model.eval()

        print("IndicTrans2 ready.")

    def _split_text(self, text: str):
        """
        Split long lesson into smaller sentences/chunks.
        """

        # Split after Hindi sentence-ending punctuation
        sentences = re.split(r'(?<=[।!?])\s*', text.strip())

        # Remove empty parts
        sentences = [
            sentence.strip()
            for sentence in sentences
            if sentence.strip()
        ]

        return sentences

    def _translate_chunk(
        self,
        text: str,
        source_language: str,
        target_language: str
    ) -> str:

        # Preprocess
        batch = self.processor.preprocess_batch(
            [text],
            src_lang=source_language,
            tgt_lang=target_language
        )

        # Tokenize
        inputs = self.tokenizer(
            batch,
            padding="longest",
            truncation=True,
            max_length=256,
            return_tensors="pt"
        ).to(self.device)

        # IndicTrans2 inference
        with torch.no_grad():
            generated_tokens = self.model.generate(
                **inputs,
                use_cache=True,
                max_length=256,
                num_beams=5,
                num_return_sequences=1
            )

        # Decode
        generated_tokens = self.tokenizer.batch_decode(
            generated_tokens,
            skip_special_tokens=True
        )

        # Postprocess
        translations = self.processor.postprocess_batch(
            generated_tokens,
            lang=target_language
        )

        return translations[0]

    def translate(
        self,
        text: str,
        source_language: str,
        target_language: str
    ) -> str:

        if not text or not text.strip():
            raise ValueError("Text cannot be empty.")

        # Short text → translate directly
        if len(text) < 300:

            return self._translate_chunk(
                text,
                source_language,
                target_language
            )

        # Long lesson → split into sentences
        chunks = self._split_text(text)

        translations = []

        for i, chunk in enumerate(chunks, start=1):

            print(
                f"Translating chunk {i}/{len(chunks)}..."
            )

            translated = self._translate_chunk(
                chunk,
                source_language,
                target_language
            )

            translations.append(translated)

        # Combine all translated sentences
        return "\n".join(translations)