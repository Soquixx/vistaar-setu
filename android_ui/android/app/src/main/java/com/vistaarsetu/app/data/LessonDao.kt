package com.vistaarsetu.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons ORDER BY id DESC")
    suspend fun getAllLessons(): List<LessonEntity>

    @Query("""
        SELECT * FROM lessons
        WHERE targetLanguage = :language
        ORDER BY id DESC
    """)
    suspend fun getLessonsByLanguage(
        language: String
    ): List<LessonEntity>

    @Query("""
        SELECT * FROM lessons
        WHERE title LIKE '%' || :query || '%'
        OR hindiText LIKE '%' || :query || '%'
        OR translatedText LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    suspend fun searchLessons(
        query: String
    ): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(
        lesson: LessonEntity
    )

    @Delete
    suspend fun deleteLesson(
        lesson: LessonEntity
    )

    @Query("""
        SELECT * FROM lessons
        WHERE hindiText = :hindiText
        AND targetLanguage = :language
        LIMIT 1
    """)
    suspend fun findCachedLesson(
        hindiText: String,
        language: String
    ): LessonEntity?

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Long): LessonEntity?

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun countLessons(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @androidx.room.Update
    suspend fun updateLesson(lesson: LessonEntity)
}