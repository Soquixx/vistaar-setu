from typing import Any

from generators.material_generator import generate_material
from worksheets.worksheet_generator import generate_worksheet
from flashcards.flashcard_generator import generate_flashcards


def process_lesson(lesson: dict[str, Any]) -> dict[str, Any]:
    """
    Main Learning Materials pipeline.

    Takes lesson data from the backend and generates all
    classroom learning materials.
    """

    # Step 1: Generate educational material
    material = generate_material(lesson)

    # Step 2: Generate worksheet
    worksheet = generate_worksheet(lesson, material)

    # Step 3: Generate flashcards
    flashcards = generate_flashcards(lesson, material)

    # Final integration-ready output
    return {
        "lesson_id": lesson.get("lesson_id", ""),
        "status": lesson.get("status", "draft"),

        "material": material,
        "worksheet": worksheet,
        "flashcards": flashcards
    }