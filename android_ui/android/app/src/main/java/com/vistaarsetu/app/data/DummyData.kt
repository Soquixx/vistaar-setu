package com.vistaarsetu.app.data

object DummyData {

    val sampleLessons = listOf(

        SavedLesson(
            id = 1L,
            title = "Numbers and Counting",
            grade = "Grade 1",
            subject = "Mathematics",
            targetLanguage = "Santali",
            hindiText = "एक, दो, तीन, चार, पांच",
            translatedText = "",
            localAudioPath = null,
            remoteAudioUrl = null
        ),

        SavedLesson(
            id = 2L,
            title = "Basic Words",
            grade = "Grade 1",
            subject = "Language",
            targetLanguage = "Santali",
            hindiText = "यह एक किताब है।",
            translatedText = "",
            localAudioPath = null,
            remoteAudioUrl = null
        ),

        SavedLesson(
            id = 3L,
            title = "Animals Around Us",
            grade = "Grade 2",
            subject = "EVS",
            targetLanguage = "Santali",
            hindiText = "हमारे आसपास पशु और पक्षी हैं।",
            translatedText = "",
            localAudioPath = null,
            remoteAudioUrl = null
        )
    )
}