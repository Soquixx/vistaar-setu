package com.vistaarsetu.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class SessionTurn(
    val turnNumber: Int,
    val speakerHindi: String,
    val translationSantali: String,
    val audioPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ClassroomSession(
    val sessionId: String,
    val title: String,
    val grade: String,
    val subject: String,
    val turns: List<SessionTurn> = emptyList(),
    val startTime: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val title: String,
    val grade: String,
    val subject: String,
    val startTime: Long,
    val isCompleted: Boolean
)

@Entity(tableName = "session_turns")
data class SessionTurnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,   // foreign key by value, matches SessionEntity.sessionId
    val turnNumber: Int,
    val speakerHindi: String,
    val translationSantali: String,
    val audioPath: String?,
    val timestamp: Long
)