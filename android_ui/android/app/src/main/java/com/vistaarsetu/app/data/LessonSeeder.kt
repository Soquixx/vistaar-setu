package com.vistaarsetu.app.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
object LessonSeeder {

    private const val TAG = "LessonSeeder"
    private const val SEED_FILE = "lesson_bank_verified.json"

    data class LessonSeedDto(
        val title: String,
        val grade: String,
        val subject: String,
        val targetLanguage: String,
        val hindiText: String,
        val translatedText: String,
        val localAudioPath: String? = null,   // treated as an ASSET FILENAME, e.g. "lesson_01.wav"
        val remoteAudioUrl: String? = null,
        val verificationStatus: String = "development"
    )

    private fun copyAudioAssetToLocal(context: Context, assetAudioFileName: String?): String? {
        if (assetAudioFileName.isNullOrBlank()) return null
        return try {
            val outFile = File(context.filesDir, "audio/$assetAudioFileName")
            outFile.parentFile?.mkdirs()
            if (!outFile.exists()) {
                context.assets.open("audio/$assetAudioFileName").use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
                Log.d(TAG, "Copied seed audio: $assetAudioFileName")
            }
            outFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy seed audio asset: $assetAudioFileName", e)
            null
        }
    }

    /**
     * Seeds the Room database from assets/lesson_bank_verified.json if database is currently empty.
     */
    suspend fun seedInitialDataIfNeeded(context: Context, force: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.lessonDao()

                val count = dao.countLessons()
                if (count > 0 && !force) {
                    Log.d(TAG, "Lesson database already contains $count lessons. Skipping seed.")
                    return@withContext
                }

                val jsonString = context.assets.open(SEED_FILE).bufferedReader().use { it.readText() }
                val listType = object : TypeToken<List<LessonSeedDto>>() {}.type
                val seedLessons: List<LessonSeedDto> = Gson().fromJson(jsonString, listType)

                val entities = seedLessons.map { dto ->
                    LessonEntity(
                        title = dto.title,
                        grade = dto.grade,
                        subject = dto.subject,
                        targetLanguage = dto.targetLanguage,
                        hindiText = dto.hindiText,
                        translatedText = dto.translatedText,
                        localAudioPath = copyAudioAssetToLocal(context, dto.localAudioPath), // <-- the actual fix
                        remoteAudioUrl = dto.remoteAudioUrl,
                        verificationStatus = dto.verificationStatus,
                        timestamp = System.currentTimeMillis()
                    )
                }

                dao.insertAll(entities)
                Log.d(TAG, "Successfully seeded ${entities.size} lessons into Room database.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed lessons from assets", e)
            }
        }
    }
}