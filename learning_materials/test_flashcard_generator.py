import json
from pathlib import Path

from flashcards.flashcard_generator import generate_flashcards


LESSON_FILE = (
    Path(__file__).resolve().parent
    / "lessons"
    / "sample_lesson.json"
)


def main():
    with open(LESSON_FILE, "r", encoding="utf-8") as file:
        lesson = json.load(file)

    flashcards = generate_flashcards(lesson, {})

    print("\nGENERATED FLASHCARDS\n")

    for card in flashcards:
        print(f"Card ID: {card['card_id']}")
        print(f"Front: {card['front']}")
        print(f"Hindi: {card['back']['hindi']}")
        print(f"Santali: {card['back']['santali'] or 'Translation pending'}")
        print(f"Image Hint: {card['image_hint']}")
        print(f"Review Status: {card['review_status']}")
        print("-" * 30)


if __name__ == "__main__":
    main()