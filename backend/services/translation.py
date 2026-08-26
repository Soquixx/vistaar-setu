import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor


MODEL_NAME = "ai4bharat/indictrans2-indic-indic-dist-320M"


class TranslationService:

    def __init__(self):
        # Device
        self.device = "cuda" if torch.cuda.is_available() else "cpu"

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

    def translate(
        self,
        text: str,
        source_language: str,
        target_language: str
    ) -> str:

        if not text or not text.strip():
            raise ValueError("Text cannot be empty.")

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