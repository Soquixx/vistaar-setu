import re
from typing import List

from schemas.lesson import Flashcard, WorksheetQuestion


class ActivityGenerator:
    """
    Generates classroom activities dynamically from
    the generated lesson content.

    No additional AI model is loaded here.
    """

    def generate_flashcards(
        self,
        source_text: str,
        translated_text: str
    ) -> List[Flashcard]:

        if not source_text.strip() or not translated_text.strip():
            return []

        source_sentences = self._split_sentences(
            source_text
        )

        translated_sentences = self._split_sentences(
            translated_text
        )

        flashcards: List[Flashcard] = []

        count = min(
            len(source_sentences),
            len(translated_sentences)
        )

        for index in range(count):

            source = source_sentences[index]
            translated = translated_sentences[index]

            if not source or not translated:
                continue

            flashcards.append(
                Flashcard(
                    front=source,
                    back=translated,
                    language="Santali"
                )
            )

        if not flashcards:

            flashcards.append(
                Flashcard(
                    front=source_text.strip(),
                    back=translated_text.strip(),
                    language="Santali"
                )
            )

        return flashcards

    def generate_worksheet(
        self,
        source_text: str,
        translated_text: str,
        grade: int,
        learning_area: str,
        learning_objective: str
    ) -> List[WorksheetQuestion]:

        if not source_text.strip():
            return []

        questions: List[WorksheetQuestion] = []

        area = learning_area.strip().lower()

        # -------------------------------------------------
        # Mathematics
        # -------------------------------------------------

        if area in {
            "mathematics",
            "math",
            "गणित"
        }:

            number_data = self._extract_number(
                source_text
            )

            if number_data is not None:

                number, original_number = number_data

                # -----------------------------------------
                # Question 1
                # Dynamically created from the actual
                # sentence and actual number.
                # -----------------------------------------

                counting_question = (
                    self._create_counting_question(
                        source_text,
                        original_number
                    )
                )

                questions.append(
                    WorksheetQuestion(
                        question_number=1,
                        question_type="multiple_choice",
                        question=counting_question,
                        options=self._generate_number_options(
                            number
                        ),
                        correct_answer=str(number),
                        points=1
                    )
                )

                # -----------------------------------------
                # Question 2
                # -----------------------------------------

                next_number = number + 1

                questions.append(
                    WorksheetQuestion(
                        question_number=2,
                        question_type="multiple_choice",
                        question=(
                            f"{number} के बाद "
                            "कौन सी संख्या आती है?"
                        ),
                        options=self._generate_next_options(
                            number
                        ),
                        correct_answer=str(next_number),
                        points=1
                    )
                )

                # -----------------------------------------
                # Question 3
                # -----------------------------------------

                previous_number = max(
                    0,
                    number - 1
                )

                questions.append(
                    WorksheetQuestion(
                        question_number=3,
                        question_type="multiple_choice",
                        question=(
                            f"{number} से पहले "
                            "कौन सी संख्या आती है?"
                        ),
                        options=self._generate_previous_options(
                            number
                        ),
                        correct_answer=str(previous_number),
                        points=1
                    )
                )

                return questions

        # -------------------------------------------------
        # General dynamic worksheet
        # -------------------------------------------------

        sentences = self._split_sentences(
            source_text
        )

        # ---------------------------------------------
        # Question 1: sentence count
        # ---------------------------------------------

        sentence_count = len(sentences)

        if sentence_count > 0:

            sentence_options = (
                self._generate_count_options(
                    sentence_count
                )
            )

            questions.append(
                WorksheetQuestion(
                    question_number=1,
                    question_type="multiple_choice",
                    question=(
                        "दिए गए पाठ में कुल कितने "
                        "वाक्य हैं?"
                    ),
                    options=sentence_options,
                    correct_answer=str(
                        sentence_count
                    ),
                    points=1
                )
            )

        # ---------------------------------------------
        # Question 2: word count
        # ---------------------------------------------

        word_count = self._count_words(
            source_text
        )

        word_options = (
            self._generate_count_options(
                word_count
            )
        )

        questions.append(
            WorksheetQuestion(
                question_number=2,
                question_type="multiple_choice",
                question=(
                    "दिए गए पाठ में कुल कितने "
                    "शब्द हैं?"
                ),
                options=word_options,
                correct_answer=str(word_count),
                points=1
            )
        )

        # ---------------------------------------------
        # Question 3: learning objective
        # ---------------------------------------------

        questions.append(
            WorksheetQuestion(
                question_number=3,
                question_type="short_answer",
                question=(
                    "इस गतिविधि का सीखने का "
                    "उद्देश्य क्या है?"
                ),
                options=[],
                correct_answer=learning_objective,
                points=1
            )
        )

        return questions

    # -----------------------------------------------------
    # Number extraction
    # -----------------------------------------------------

    @staticmethod
    def _extract_number(
        text: str
    ):

        candidates = []

        # ---------------------------------------------
        # Arabic digits
        # ---------------------------------------------

        for match in re.finditer(
            r"\d+",
            text
        ):

            candidates.append(
                (
                    match.start(),
                    int(match.group()),
                    match.group()
                )
            )

        # ---------------------------------------------
        # Devanagari digits
        # ---------------------------------------------

        devanagari_digits = str.maketrans(
            "०१२३४५६७८९",
            "0123456789"
        )

        for match in re.finditer(
            r"[०-९]+",
            text
        ):

            original = match.group()

            value = int(
                original.translate(
                    devanagari_digits
                )
            )

            candidates.append(
                (
                    match.start(),
                    value,
                    original
                )
            )

        # ---------------------------------------------
        # Hindi number words
        # ---------------------------------------------

        hindi_numbers = {
            "शून्य": 0,
            "एक": 1,
            "दो": 2,
            "तीन": 3,
            "चार": 4,
            "पाँच": 5,
            "पांच": 5,
            "छह": 6,
            "छः": 6,
            "सात": 7,
            "आठ": 8,
            "नौ": 9,
            "दस": 10
        }

        for word, value in hindi_numbers.items():

            for match in re.finditer(
                rf"(?<!\S){word}(?!\S)",
                text
            ):

                candidates.append(
                    (
                        match.start(),
                        value,
                        word
                    )
                )

        # ---------------------------------------------
        # Select the last number appearing in the text.
        #
        # Example:
        # "एक पेड़ पर सात चिड़ियाँ बैठी हैं।"
        #
        # एक  -> 1
        # सात -> 7
        #
        # Result -> 7
        # ---------------------------------------------

        if not candidates:
            return None

        candidates.sort(
            key=lambda item: item[0]
        )

        _, value, original = candidates[-1]

        return (
            value,
            original
        )

    # -----------------------------------------------------
    # Dynamic counting question
    # -----------------------------------------------------

    @staticmethod
    def _create_counting_question(
        source_text: str,
        original_number: str
    ) -> str:

        sentences = re.split(
            r"(?<=[।!?])\s*",
            source_text.strip()
        )

        for sentence in sentences:

            if original_number not in sentence:
                continue

            question = sentence.replace(
                original_number,
                "कितनी",
                1
            )

            question = question.rstrip(
                "।!?"
            )

            return question + "?"

        return (
            "दिए गए पाठ के अनुसार "
            "कुल कितनी वस्तुएँ हैं?"
        )

    # -----------------------------------------------------
    # Dynamic numeric options
    # -----------------------------------------------------

    @staticmethod
    def _generate_number_options(
        number: int
    ) -> List[str]:

        if number == 0:

            return [
                "0",
                "1",
                "2"
            ]

        if number == 1:

            return [
                "0",
                "1",
                "2"
            ]

        return [
            str(number - 1),
            str(number),
            str(number + 1)
        ]

    # -----------------------------------------------------
    # Options for next-number question
    # -----------------------------------------------------

    @staticmethod
    def _generate_next_options(
        number: int
    ) -> List[str]:

        correct = number + 1

        return [
            str(max(0, number - 1)),
            str(correct),
            str(number + 2)
        ]

    # -----------------------------------------------------
    # Options for previous-number question
    # -----------------------------------------------------

    @staticmethod
    def _generate_previous_options(
        number: int
    ) -> List[str]:

        correct = max(
            0,
            number - 1
        )

        options = [
            max(0, number - 2),
            correct,
            number + 1
        ]

        # Remove duplicates while preserving order
        unique_options = list(
            dict.fromkeys(options)
        )

        # Ensure three options
        candidate = number + 2

        while len(unique_options) < 3:

            if candidate not in unique_options:
                unique_options.append(
                    candidate
                )

            candidate += 1

        return [
            str(value)
            for value in unique_options
        ]

    # -----------------------------------------------------
    # Dynamic count options
    # -----------------------------------------------------

    @staticmethod
    def _generate_count_options(
        correct_value: int
    ) -> List[str]:

        if correct_value <= 1:

            return [
                str(correct_value),
                str(correct_value + 1),
                str(correct_value + 2)
            ]

        return [
            str(correct_value - 1),
            str(correct_value),
            str(correct_value + 1)
        ]

    # -----------------------------------------------------
    # Word counting
    # -----------------------------------------------------

    @staticmethod
    def _count_words(
        text: str
    ) -> int:

        words = re.findall(
            r"\S+",
            text.strip()
        )

        return len(words)

    # -----------------------------------------------------
    # Sentence splitting
    # -----------------------------------------------------

    @staticmethod
    def _split_sentences(
        text: str
    ) -> List[str]:
        """
        Split Hindi / Indic / Ol Chiki text using
        common sentence-ending punctuation.
        """

        sentences = re.split(
            r"(?<=[।!?᱾])\s*",
            text.strip()
        )

        return [
            sentence.strip()
            for sentence in sentences
            if sentence.strip()
        ]