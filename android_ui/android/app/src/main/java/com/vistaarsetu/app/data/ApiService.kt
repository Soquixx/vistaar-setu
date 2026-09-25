package com.vistaarsetu.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class ProcessLessonRequest(
    val grade: Int,
    val learning_area: String,
    val learning_objective: String,
    val target_language: String,
    val text: String
)

data class ProcessLessonResponse(
    val source_text: String,
    val target_language: String,
    val translated_text: String,
    val audio_url: String?,
    val status: String,
    val localAudioPath: String? = null
)

interface ApiService {
    @POST("lesson/process")
    suspend fun processLesson(@Body request: ProcessLessonRequest): ProcessLessonResponse
}

object RetrofitClient {

    private const val BASE_URL = "Ipv4 address"
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

    fun getFullAudioUrl(relativePath: String?): String? {
        if (relativePath == null) return null
        val cleanPath = relativePath.removePrefix("/")
        return "$BASE_URL$cleanPath"
    }
}