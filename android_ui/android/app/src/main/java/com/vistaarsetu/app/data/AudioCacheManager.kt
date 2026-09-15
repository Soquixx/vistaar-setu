package com.vistaarsetu.app.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object AudioCacheManager {
    suspend fun downloadAudio(
        context: Context,
        audioUrl: String
    ): String? {
        return try {
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
                return null
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
     * Checks whether a previously downloaded audio file still exists.
     */
    fun isAvailable(localPath: String?): Boolean {
        if (localPath.isNullOrBlank()) return false
        return File(localPath).exists()
    }
}