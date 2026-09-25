package com.vistaarsetu.app.data

import android.content.Context
import android.util.Log

class LessonRepository(
    private val context: Context
) {

    private val db =
        AppDatabase.getDatabase(context)

    private val dao =
        db.lessonDao()

    private fun normalizeText(text: String): String {
        return text
            .trim()
            .replace(Regex("[।.,!?\"'‘’“”]"), "")
            .replace(Regex("\\s+")," ")
            .trim()
    }

    private fun isMatchingLanguage(savedLang: String, requestedLang: String): Boolean {
        val s = savedLang.lowercase()
        val r = requestedLang.lowercase()
        return s.contains(r) || r.contains(s) ||
                (s.contains("sat") && r.contains("sat")) ||
                (s.contains("ho") && r.contains("ho")) ||
                (s.contains("mun") && r.contains("mun"))
    }

    suspend fun getLesson(
        request: ProcessLessonRequest,
        subject: String
    ): ProcessLessonResponse {

        val normalizedText =
            normalizeText(request.text)

        // 1. CACHE FIRST (Room database lookup)
        val allLessons = dao.getAllLessons()
        val reqGradeDigits = request.grade.toString()

        val cached = allLessons.firstOrNull { lesson ->
            val textMatches = normalizeText(lesson.hindiText).equals(normalizedText, ignoreCase = true)
            val langMatches = isMatchingLanguage(lesson.targetLanguage, request.target_language)
            val gradeMatches = lesson.grade.filter { it.isDigit() } == reqGradeDigits
            textMatches && langMatches && (gradeMatches || lesson.grade.isBlank())
        } ?: allLessons.firstOrNull { lesson ->
            val textMatches = normalizeText(lesson.hindiText).equals(normalizedText, ignoreCase = true)
            val langMatches = isMatchingLanguage(lesson.targetLanguage, request.target_language)
            textMatches && langMatches
        }

        // 2. RETURN CACHED RESULT IMMEDIATELY IF FOUND
        if (cached != null) {
            val isLocalAudioValid = AudioCacheManager.isAvailable(cached.localAudioPath)

            return ProcessLessonResponse(
                source_text = cached.hindiText,
                target_language = cached.targetLanguage,
                translated_text = cached.translatedText,
                audio_url = cached.remoteAudioUrl ?: "",
                status = "success_cached",
                localAudioPath = if (isLocalAudioValid) cached.localAudioPath else null
            )
        }

        // 3. CACHE MISS → CALL FASTAPI BACKEND
        val response = try {
            RetrofitClient.apiService.processLesson(request)
        } catch (e: retrofit2.HttpException) {
            Log.e("LESSON_REPO", "Backend rejected request: ${e.code()} - ${e.response()?.errorBody()?.string()}", e)
            throw OfflineLessonUnavailableException("Translation request failed (${e.code()}). Please try again.")
        } catch (e: Exception) {
            Log.e("LESSON_REPO", "Backend unreachable", e)
            throw OfflineLessonUnavailableException("This sentence is not cached locally. Network connection required.")
        }

        // 4. BUILD COMPLETE AUDIO URL
        val fullAudioUrl =
            response.audio_url?.let { remotePath ->
                if (
                    remotePath.startsWith("http://") ||
                    remotePath.startsWith("https://")
                ) {
                    remotePath
                } else {
                    RetrofitClient.getFullAudioUrl(remotePath)
                }
            }

        // 5. DOWNLOAD AUDIO TO DEVICE FOR OFFLINE USE
        val localAudioPath =
            fullAudioUrl?.let { url ->
                AudioCacheManager.downloadAudio(
                    context = context,
                    audioUrl = url
                )
            }

        // 6. SAVE IN ROOM CACHE
        dao.insertLesson(
            LessonEntity(
                title = response.source_text.take(40),
                grade = "Grade ${request.grade}",
                subject = subject,
                targetLanguage = response.target_language,
                hindiText = response.source_text,
                translatedText = response.translated_text,
                localAudioPath = localAudioPath,
                remoteAudioUrl = fullAudioUrl,
                verificationStatus = "development"
            )
        )

        // 7. RETURN PROCESSED RESPONSE
        return response.copy(
            localAudioPath = localAudioPath
        )
    }
}

class OfflineLessonUnavailableException(message: String) : Exception(message)