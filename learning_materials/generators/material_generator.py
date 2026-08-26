import json
from pathlib import Path
from typing import Any


BASE_DIR = Path(__file__).resolve().parent.parent

RULES_FILE = BASE_DIR / "lessons" / "content_rules.json"
OUTCOMES_FILE = BASE_DIR / "lessons" / "learning_outcomes.json"


def load_json(file_path: Path) -> dict[str, Any]:
    with open(file_path, "r", encoding="utf-8") as file:
        return json.load(file)


def find_matching_rule(
    source_text: str,
    rules: dict[str, Any]
) -> tuple[str, dict[str, Any]] | None:

    source_text_lower = source_text.lower()

    for rule_name, rule in rules.items():
        for keyword in rule.get("keywords", []):
            if keyword.lower() in source_text_lower:
                return rule_name, rule

    return None


def generate_material(lesson: dict[str, Any]) -> dict[str, Any]:

    source_text = lesson.get("source_text", "")

    if not source_text:
        raise ValueError("source_text is required.")

    rules = load_json(RULES_FILE)
    outcomes = load_json(OUTCOMES_FILE)

    match = find_matching_rule(source_text, rules)

    if match:
        topic, rule = match
        outcome = outcomes.get(topic, {}).get("learning_outcome", "")

        return {
            "topic": topic,
            "learning_outcome": outcome,
            "learning_objective": rule["learning_objective"],
            "classroom_activity": rule["classroom_activity"],
            "assessment_prompts": rule["assessment_prompts"]
        }

    grade = lesson.get("grade", "")

    return {
        "topic": "general",
        "learning_outcome": "",
        "learning_objective": f"Students will understand the main concept of the Grade {grade} lesson.",
        "classroom_activity": "The teacher explains the concept and guides students through a simple interactive activity.",
        "assessment_prompts": [
            "Ask a simple question related to the lesson.",
            "Observe the student's participation and response."
        ]
    }