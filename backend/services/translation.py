import re

import torch

from transformers import (
    AutoModelForSeq2SeqLM,
    AutoTokenizer
)

from IndicTransToolkit import IndicProcessor


MODEL_NAME = (
    "ai4bharat/"
    "indictrans2-indic-indic-dist-320M"
)


class TranslationService:

    def __init__(self):

        self.device = (
            "cuda"
            if torch.cuda.is_available()
            else "cpu"
        )

        print(
            f"TranslationService device: "
            f"{self.device}"
        )

        self.processor = IndicProcessor(
            inference=True
        )

        self.tokenizer = (
            AutoTokenizer.from_pretrained(
                MODEL_NAME,
                trust_remote_code=True
            )
        )

        self.model = (
            AutoModelForSeq2SeqLM
            .from_pretrained(
                MODEL_NAME,
                trust_remote_code=True
            )
            .to(self.device)
        )

        self.model.eval()

        print(
            "IndicTrans2 ready."
        )

    def _split_text(
        self,
        text: str
    ):

        sentences = re.split(
            r'(?<=[।!?])\s*',
            text.strip()
        )

        return [
            sentence.strip()
            for sentence in sentences
            if sentence.strip()
        ]

    def _translate_chunk(
        self,
        text: str,
        source_language: str,
        target_language: str
    ) -> str:

        batch = (
            self.processor.preprocess_batch(
                [text],
                src_lang=source_language,
                tgt_lang=target_language
            )
        )

        inputs = self.tokenizer(
            batch,
            padding="longest",
            truncation=True,
            max_length=256,
            return_tensors="pt"
        ).to(self.device)

        with torch.no_grad():

            generated_tokens = (
                self.model.generate(
                    **inputs,
                    use_cache=True,
                    max_length=256,
                    num_beams=1,
                    num_return_sequences=1
                )
            )

        generated_tokens = (
            self.tokenizer.batch_decode(
                generated_tokens,
                skip_special_tokens=True
            )
        )

        translations = (
            self.processor.postprocess_batch(
                generated_tokens,
                lang=target_language
            )
        )

        if not translations:
            raise RuntimeError(
                "Translation model returned "
                "no translation."
            )

        return translations[0].strip()

    def translate(
        self,
        text: str,
        source_language: str,
        target_language: str
    ) -> str:

        if not text or not text.strip():
            raise ValueError(
                "Text cannot be empty."
            )

        text = text.strip()

        if len(text) < 300:

            return self._translate_chunk(
                text,
                source_language,
                target_language
            )

        chunks = self._split_text(text)

        translations = []

        for index, chunk in enumerate(
            chunks,
            start=1
        ):

            print(
                f"Translating chunk "
                f"{index}/{len(chunks)}..."
            )

            translated = (
                self._translate_chunk(
                    chunk,
                    source_language,
                    target_language
                )
            )

            translations.append(
                translated
            )

        return "\n".join(
            translations
        )