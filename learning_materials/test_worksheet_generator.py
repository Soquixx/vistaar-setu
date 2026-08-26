import json
from pathlib import Path

from generators.material_generator import generate_material
from worksheets.worksheet_generator import generate_worksheet


LESSON_FILE = (
    Path(__file__).resolve().parent
    / "lessons"
    / "sample_lesson.json"
)


def main():
    with open(LESSON_FILE, "r", encoding="utf-8") as file:
        lesson = json.load(file)

    material = generate_material(lesson)
    worksheet = generate_worksheet(lesson, material)

    print("\nGENERATED WORKSHEET\n")
    print(f"Title: {worksheet['title']}")
    print(f"Grade: {worksheet['grade']}")
    print(f"Learning Area: {worksheet['learning_area']}")

    print("\nTopic:")
    print(worksheet["topic"])

    print("\nLearning Outcome:")
    print(worksheet["learning_outcome"])

    print("\nLearning Objective:")
    print(worksheet["learning_objective"])

    print("\nHindi Lesson:")
    print(worksheet["lesson_content"]["hindi"])

    print("\nSantali Lesson:")
    print(
        worksheet["lesson_content"]["santali"]
        or "Translation will be provided by the backend."
    )

    print("\nActivity:")
    print(worksheet["activity"])

    print("\nQuestions:")
    for number, question in enumerate(worksheet["questions"], start=1):
        print(f"{number}. {question}")

    print("\nReview Status:")
    print(worksheet["review_status"])


if __name__ == "__main__":
    main()