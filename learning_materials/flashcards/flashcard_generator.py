from typing import Any


def generate_flashcards(
    lesson: dict[str, Any],
    material: dict[str, Any]
) -> list[dict[str, Any]]:

    source_text = lesson.get("source_text", "")
    translated_text = lesson.get("translated_text", "")
    audio_file = lesson.get("audio_file", "")

    if not source_text:
        raise ValueError("source_text is required to generate flashcards.")

    flashcards = []

    if any(keyword in source_text.lower()
           for keyword in ["गिनो", "गिनती", "count", "counting"]):

        hindi_numbers = ["एक", "दो", "तीन", "चार", "पाँच"]

        for index, hindi_number in enumerate(hindi_numbers, start=1):
            flashcards.append(
                {
                    "card_id": f"count-{index}",
                    "type": "number",
                    "front": str(index),
                    "back": {
                        "hindi": hindi_number,
                        "santali": ""
                    },
                    "audio_file": audio_file,
                    "image_hint": f"{index} objects",
                    "review_status": "draft"
                }
            )

    if not flashcards:
        flashcards.append(
            {
                "card_id": "lesson-1",
                "type": "concept",
                "front": source_text,
                "back": {
                    "hindi": source_text,
                    "santali": translated_text
                },
                "audio_file": audio_file,
                "image_hint": "",
                "review_status": "draft"
            }
        )

    return flashcards