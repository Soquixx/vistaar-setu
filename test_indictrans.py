import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor


MODEL_NAME = "ai4bharat/indictrans2-indic-indic-dist-320M"

SRC_LANG = "hin_Deva"
TGT_LANG = "sat_Olck"


def main():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Device: {device}")

    print("Loading IndicProcessor...")
    processor = IndicProcessor(inference=True)

    print("Loading IndicTrans2 model...")

    tokenizer = AutoTokenizer.from_pretrained(
        MODEL_NAME,
        trust_remote_code=True
    )

    model = AutoModelForSeq2SeqLM.from_pretrained(
        MODEL_NAME,
        trust_remote_code=True
    ).to(device)

    model.eval()

    print("Model loaded successfully.")

    text = "गिनो: एक, दो, तीन, चार, पाँच।"

    print("\nHindi:")
    print(text)

    # IndicTrans2 preprocessing
    batch = processor.preprocess_batch(
        [text],
        src_lang=SRC_LANG,
        tgt_lang=TGT_LANG
    )

    # Tokenize using Hugging Face tokenizer
    inputs = tokenizer(
        batch,
        padding="longest",
        truncation=True,
        return_tensors="pt"
    ).to(device)

    # Generate translation
    with torch.no_grad():
        generated_tokens = model.generate(
            **inputs,
            use_cache=True,
            max_length=256,
            num_beams=5,
            num_return_sequences=1
        )

    # Decode
    generated_tokens = tokenizer.batch_decode(
        generated_tokens,
        skip_special_tokens=True
    )

    # IndicTrans2 postprocessing
    translations = processor.postprocess_batch(
        generated_tokens,
        lang=TGT_LANG
    )

    print("\nSantali:")
    print(translations[0])


if __name__ == "__main__":
    main()