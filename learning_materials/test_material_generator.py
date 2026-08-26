import json
from pathlib import Path

from generators.material_generator import generate_material


LESSON_FILE = (
    Path(__file__).resolve().parent
    / "lessons"
    / "sample_lesson.json"
)


def main():
    # Load sample lesson data
    with open(LESSON_FILE, "r", encoding="utf-8") as file:
        lesson = json.load(file)

    # Generate learning materials
    material = generate_material(lesson)

    print("\nGENERATED LEARNING MATERIAL\n")

    print("Topic:")
    print(material["topic"])

    print("\nLearning Outcome:")
    print(material["learning_outcome"])

    print("\nLearning Objective:")
    print(material["learning_objective"])

    print("\nClassroom Activity:")
    print(material["classroom_activity"])

    print("\nAssessment Prompts:")
    for number, prompt in enumerate(material["assessment_prompts"], start=1):
        print(f"{number}. {prompt}")


if __name__ == "__main__":
    main()