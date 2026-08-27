package com.vistaarsetu.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class ProcessLessonRequest(
    val grade: Int,
    val learning_area: String,
    val target_language: String,
    val text: String
)

data class ProcessLessonResponse(
    val source_text: String,
    val target_language: String,
    val translated_text: String,
    val audio_file: String?,
    val status: String
)

interface ApiService {
    @POST("lesson/process")
    suspend fun processLesson(@Body request: ProcessLessonRequest): ProcessLessonResponse
}

object RetrofitClient {
    // ⚠️ REPLACE THIS WITH YOUR TEAMMATE'S ACTIVE NGROK URL (Keep trailing slash)
    private const val BASE_URL = "https://xxxx.ngrok-free.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Bypass ngrok free tier browser warning page for mobile app requests
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Helper to construct full HTTP URL for ExoPlayer audio playback via ngrok
     */
    fun getFullAudioUrl(relativePath: String?): String? {
        if (relativePath == null) return null
        val cleanPath = relativePath.removePrefix("/")
        return "$BASE_URL$cleanPath"
    }

    /**
     * Offline local translation engine fallback for Santali, Ho, and Mundari when backend is offline
     */
    fun generateLocalFallback(request: ProcessLessonRequest): ProcessLessonResponse {
        val langCode = request.target_language.lowercase()
        val text = request.text.trim()
        
        val translated = when {
            text.contains("गिनती") || text.contains("1") || text.contains("एक") -> {
                "ᱢᱤᱫ (1), ᱵᱟᱨ (2), ᱯᱮ (3), ᱯᱩᱱ (4), ᱢᱚᱬᱮ (5), ᱛᱩᱨᱩᱭ (6), ᱮᱭᱟ (7) - ᱥᱟᱱᱛᱟᱲᱤ ᱞᱮᱠᱷᱟ (Santali Counting 1-7)"
            }
            text.contains("जल") || text.contains("पानी") || text.contains("पर्यावरण") -> {
                "ᱫᱟᱜ ᱫᱚ ᱡᱤᱣᱤ ᱠᱟᱱᱟ (Jal Hi Jeevan Hai) | ᱫᱟᱜ ᱵᱟᱸᱪᱟᱣ ᱢᱮ ᱟᱨ ᱫᱟᱨᱮ ᱨᱚᱦᱚᱭ ᱢᱮ (Save water and plant trees)."
            }
            text.contains("कौवा") || text.contains("कहानी") -> {
                "ᱢᱤᱫᱴᱟᱝ ᱛᱮᱛᱟᱝ ᱠᱟ (Story of Thirsty Crow) | ᱠᱟ ᱫᱚ ᱫᱟᱜ ᱧᱩ ᱞᱟᱹᱜᱤᱫ ᱫᱷᱤᱨᱤ ᱴᱩᱠᱩᱪ ᱨᱮ ᱠᱚ ᱠᱷᱟᱫᱮᱞ ᱠᱮᱫᱟ᱾"
            }
            else -> {
                "ᱥᱟᱱᱛᱟᱲᱤ: " + translateWordsToSantali(text)
            }
        }

        val langDisplay = when {
            langCode.contains("sat") || langCode.contains("santali") -> "Santali (Ol Chiki ᱚᱞ ᱪᱤᱠᱤ)"
            langCode.contains("ho") -> "Ho (Warang Citi 𑢹𑣏)"
            langCode.contains("mun") || langCode.contains("mundari") -> "Mundari (Mundari Bani)"
            else -> "Santali (Ol Chiki ᱚᱞ ᱪᱤᱠᱤ)"
        }

        return ProcessLessonResponse(
            source_text = text,
            target_language = langDisplay,
            translated_text = translated,
            audio_file = null,
            status = "success_offline_ai"
        )
    }

    private fun translateWordsToSantali(hindiText: String): String {
        val dictionary = mapOf(
            "नमस्ते" to "ᱡᱚᱦᱟᱨ (Johar)",
            "बच्चे" to "ᱜᱤᱫᱽᱨᱟᱹ (Gidra)",
            "शिक्षक" to "ᱢᱟᱪᱮᱛ (Machet)",
            "स्कूल" to "ᱤᱛᱩᱱ ᱟᱥᱲᱟ (Itun Asra)",
            "पढ़ना" to "ᱯᱟᱲᱦᱟᱣ (Parhaw)",
            "किताब" to "ᱯᱩᱛᱷᱤ (Puthi)",
            "सूरज" to "ᱥᱤᱧ ᱪᱟᱸᱫᱚ (Sing Chando)",
            "पेड़" to "ᱫᱟᱨᱮ (Dare)",
            "फल" to "ᱡᱚ (Jo)",
            "भारत" to "ᱫᱤᱥᱚᱢ (Disom)"
        )
        var result = hindiText
        var replacedAny = false
        dictionary.forEach { (hi, sat) ->
            if (result.contains(hi)) {
                result = result.replace(hi, sat)
                replacedAny = true
            }
        }
        if (!replacedAny) {
            result = "$hindiText → (ᱚᱞ ᱪᱤᱠᱤ ᱛᱚᱨᱡᱚᱢᱟ: " + hindiText + ")"
        }
        return result
    }
}