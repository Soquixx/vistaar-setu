package com.vistaarsetu.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vistaarsetu.app.data.models.SessionEntity
import com.vistaarsetu.app.data.models.SessionTurnEntity


@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Insert
    suspend fun insertTurn(turn: SessionTurnEntity)

    @Query("SELECT * FROM session_turns WHERE sessionId = :sessionId ORDER BY turnNumber ASC")
    suspend fun getTurnsForSession(sessionId: String): List<SessionTurnEntity>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Update
    suspend fun updateSession(session: SessionEntity)
}

