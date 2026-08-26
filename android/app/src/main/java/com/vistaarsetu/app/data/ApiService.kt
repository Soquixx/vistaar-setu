package com.vistaarsetu.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

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
    @POST("lessons/process")
    suspend fun processLesson(@Body request: ProcessLessonRequest): ProcessLessonResponse
}

object RetrofitClient {
    // 10.0.2.2 routes emulator traffic directly to localhost where FastAPI runs
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}