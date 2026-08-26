import json
from pathlib import Path

from learning_materials_pipeline import process_lesson


LESSON_FILE = (
    Path(__file__).resolve().parent
    / "lessons"
    / "sample_lesson.json"
)


def main():
    # Load the default sample lesson
    with open(LESSON_FILE, "r", encoding="utf-8") as file:
        lesson = json.load(file)

    # Run complete learning materials pipeline
    result = process_lesson(lesson)

    print("\nCOMPLETE LEARNING MATERIALS PIPELINE\n")

    print("Lesson ID:")
    print(result["lesson_id"])

    print("\nStatus:")
    print(result["status"])

    print("\nTopic:")
    print(result["material"]["topic"])

    print("\nLearning Outcome:")
    print(result["material"]["learning_outcome"])

    print("\nLearning Objective:")
    print(result["material"]["learning_objective"])

    print("\nWorksheet Title:")
    print(result["worksheet"]["title"])

    print("\nFlashcards Generated:")
    print(len(result["flashcards"]))

    print("\nPIPELINE COMPLETED SUCCESSFULLY")


if __name__ == "__main__":
    main()
    