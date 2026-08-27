package com.vistaarsetu.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "saved_lessons")
data class SavedLesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val grade: String,
    val subject: String,
    val targetLanguage: String,
    val hindiText: String,
    val translatedText: String,
    val localAudioPath: String? = null,
    val remoteAudioUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface LessonDao {
    @Query("SELECT * FROM saved_lessons ORDER BY id DESC")
    suspend fun getAllLessons(): List<SavedLesson>

    @Query("SELECT * FROM saved_lessons WHERE targetLanguage = :language ORDER BY id DESC")
    suspend fun getLessonsByLanguage(language: String = "Santhali"): List<SavedLesson>

    @Query("SELECT * FROM saved_lessons WHERE title LIKE '%' || :query || '%' OR hindiText LIKE '%' || :query || '%' OR translatedText LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchLessons(query: String): List<SavedLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: SavedLesson)

    @Delete
    suspend fun deleteLesson(lesson: SavedLesson)
}

@Database(entities = [SavedLesson::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vistaar_setu_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}