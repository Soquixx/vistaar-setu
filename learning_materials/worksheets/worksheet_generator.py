from typing import Any


def generate_worksheet(
    lesson: dict[str, Any],
    material: dict[str, Any]
) -> dict[str, Any]:

    if not lesson.get("source_text"):
        raise ValueError("source_text is required.")

    worksheet = {
        "title": "Learning Worksheet",
        "grade": lesson.get("grade"),
        "learning_area": lesson.get("learning_area"),
        "target_language": lesson.get("target_language"),

        "topic": material.get("topic", ""),
        "learning_outcome": material.get("learning_outcome", ""),
        "learning_objective": material.get("learning_objective", ""),

        "lesson_content": {
            "hindi": lesson.get("source_text", ""),
            "santali": lesson.get("translated_text", "")
        },

        "activity": material.get("classroom_activity", ""),
        "questions": material.get("assessment_prompts", []),

        "review_status": "draft"
    }

    return worksheet