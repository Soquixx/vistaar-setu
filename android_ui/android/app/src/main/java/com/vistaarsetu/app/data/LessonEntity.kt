package com.vistaarsetu.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,

    val grade: String,

    val subject: String,

    val targetLanguage: String,

    val hindiText: String,

    val translatedText: String,

    val localAudioPath: String? = null,

    val remoteAudioUrl: String? = null,

    val verificationStatus: String = "development",

    val timestamp: Long = System.currentTimeMillis()
)

