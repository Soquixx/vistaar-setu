package com.vistaarsetu.app.data

object DummyData {

    val sampleLessons = listOf(
        SavedLesson(
            id = 1L,
            title = "Numbers and Counting",
            grade = "Grade 1",
            subject = "Mathematics",
            targetLanguage = "Santhali",
            hindiText = "1 से 10 तक गिनती सीखें। एक, दो, तीन, चार...",
            translatedText = "᱑ ᱠᱷᱚᱱ ᱑᱐ ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ। ᱢᱤᱫ, ᱵᱟᱨ, ᱯᱮ, ᱯᱩᱱ...",
            localAudioPath = null,
            remoteAudioUrl = "https://example.com/audio/numbers_santhali.mp3",
            timestamp = System.currentTimeMillis()
        ),
        SavedLesson(
            id = 2L,
            title = "Basic Alphabets & Words",
            grade = "Grade 2",
            subject = "Language Arts",
            targetLanguage = "Santhali",
            hindiText = "जल ही जीवन है। पेड़-पौधे हमें ऑक्सीजन देते हैं।",
            translatedText = "ᱫᱟ structure ᱜᱮ ᱡᱤᱣᱤ ᱠᱟᱱᱟ। ᱫᱟᱨᱮ-ᱱᱟᱹᱲᱤ ᱵᱚᱱ ᱚᱠᱥᱤᱡᱮᱱ ᱠᱚ ᱮᱢᱟᱵᱚᱱᱟ।",
            localAudioPath = null,
            remoteAudioUrl = null,
            timestamp = System.currentTimeMillis()
        ),
        SavedLesson(
            id = 3L,
            title = "Environmental Science Intro",
            grade = "Grade 3",
            subject = "EVS",
            targetLanguage = "Santhali",
            hindiText = "हमारे आसपास के पशु और पक्षी।",
            translatedText = "ᱟᱵᱚ ᱟ design ᱠᱚᱨᱮᱱ ᱡᱤᱣᱤᱟᱹᱞᱤ ᱟᱨ ᱪᱮᱬᱮ ᱠᱚ।",
            localAudioPath = null,
            remoteAudioUrl = null,
            timestamp = System.currentTimeMillis()
        )
    )
}