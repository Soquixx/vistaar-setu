package com.vistaarsetu.app.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AudioCacheManager {
    suspend fun downloadAudio(
        context: Context,
        audioUrl: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "audio_${System.currentTimeMillis()}.wav"
            val audioDir = File(context.filesDir, "audio")

            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val audioFile = File(audioDir, fileName)

            val connection =
                URL(audioUrl).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.setRequestProperty(
                "ngrok-skip-browser-warning",
                "true"
            )

            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            connection.inputStream.use { input ->
                FileOutputStream(audioFile).use { output ->
                    input.copyTo(output)
                }
            }

            connection.disconnect()

            audioFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks whether a previously downloaded audio file still exists and has valid audio data.
     */
    fun isAvailable(localPath: String?): Boolean {
        if (localPath.isNullOrBlank()) return false
        val file = File(localPath)
        return file.exists() && file.length() > 0
    }
}