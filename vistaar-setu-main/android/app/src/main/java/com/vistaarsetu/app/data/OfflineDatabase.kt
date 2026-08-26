package com.vistaarsetu.app.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "offline_lessons")
data class SavedLesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val grade: String,
    val targetLanguage: String,
    val hindiText: String,
    val translatedText: String,
    val audioUrl: String?
)

@Dao
interface LessonDao {
    @Query("SELECT * FROM offline_lessons ORDER BY id DESC")
    suspend fun getAllLessons(): List<SavedLesson>

    @Query("SELECT * FROM offline_lessons WHERE title LIKE '%' || :query || '%' OR hindiText LIKE '%' || :query || '%' OR translatedText LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchLessons(query: String): List<SavedLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: SavedLesson)

    @Delete
    suspend fun deleteLesson(lesson: SavedLesson)
}

@Database(entities = [SavedLesson::class], version = 1, exportSchema = false)
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