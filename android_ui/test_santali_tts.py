import torch
import soundfile as sf
from transformers import AutoTokenizer, AutoFeatureExtractor
from parler_tts import ParlerTTSForConditionalGeneration


MODEL_NAME = "ai4bharat/indic-parler-tts"


def main():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Device: {device}")

    print("Loading tokenizer...")
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

    print("Loading feature extractor...")
    feature_extractor = AutoFeatureExtractor.from_pretrained(MODEL_NAME)

    print("Loading Indic Parler-TTS model...")
    model = ParlerTTSForConditionalGeneration.from_pretrained(
        MODEL_NAME
    ).to(device)

    model.eval()

    print("Model loaded successfully.")

    # Santali text from your verified translation output
    prompt = (
        "ᱜᱤᱱᱳᱼ ᱢᱤᱫᱴᱟᱝ, ᱵᱟᱨᱭᱟ, ᱯᱮᱭᱟ, "
        "ᱯᱳᱱ ᱜᱚᱴᱟᱝ, ᱢᱚᱬᱮ ᱜᱚᱴᱟᱝ ᱾"
    )

    description = (
        "Arjun speaks naturally in Santali with a clear, "
        "friendly teaching voice. The recording is very high "
        "quality with no background noise."
    )

    print("Generating Santali audio...")

    # Tokenize speaker/style description
    description_input_ids = tokenizer(
        description,
        return_tensors="pt"
    ).input_ids.to(device)

    # Tokenize Santali transcript
    prompt_input_ids = tokenizer(
        prompt,
        return_tensors="pt"
    ).input_ids.to(device)

    with torch.no_grad():
        generation = model.generate(
            input_ids=description_input_ids,
            prompt_input_ids=prompt_input_ids
        )

    audio_arr = generation.cpu().numpy().squeeze()

    output_file = "santali_test.wav"

    sf.write(
        output_file,
        audio_arr,
        model.config.sampling_rate
    )

    print(f"Audio generated successfully: {output_file}")


if __name__ == "__main__":
    main()