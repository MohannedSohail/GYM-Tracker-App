package com.gymtracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SessionParticipantCrossRef
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // --- Workout Sessions ---
    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET endTime = :endTime WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, endTime: Long)

    // --- Session Participants ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionParticipants(crossRefs: List<SessionParticipantCrossRef>)

    @Query("""
        SELECT p.* FROM participants p
        INNER JOIN session_participants sp ON p.id = sp.participantId
        WHERE sp.sessionId = :sessionId
        ORDER BY p.id ASC
    """)
    fun getParticipantsForSession(sessionId: Long): Flow<List<ParticipantEntity>>

    @Query("SELECT participantId FROM session_participants WHERE sessionId = :sessionId")
    suspend fun getParticipantIdsForSession(sessionId: Long): List<Long>

    // --- Set Logs (Rounds) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(setLog: SetLogEntity): Long

    @Update
    suspend fun updateSetLog(setLog: SetLogEntity)

    @Query("DELETE FROM set_logs WHERE id = :setLogId")
    suspend fun deleteSetLog(setLogId: Long)

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForSessionAndExercise(sessionId: Long, exerciseId: Long): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY loggedAt ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<SetLogEntity>>

    @Query("""
        SELECT * FROM set_logs 
        WHERE participantId = :participantId AND exerciseId = :exerciseId
        ORDER BY loggedAt DESC LIMIT 1
    """)
    suspend fun getLastSetForParticipantAndExercise(participantId: Long, exerciseId: Long): SetLogEntity?

    @Query("""
        SELECT * FROM set_logs 
        WHERE sessionId = :sessionId AND exerciseId = :exerciseId AND participantId = :participantId 
        ORDER BY setNumber ASC
    """)
    suspend fun getSetsForParticipantInExercise(
        sessionId: Long,
        exerciseId: Long,
        participantId: Long
    ): List<SetLogEntity>

    @Transaction
    suspend fun deleteAndReindexSet(
        setLogId: Long,
        sessionId: Long,
        exerciseId: Long,
        participantId: Long
    ) {
        deleteSetLog(setLogId)
        val remainingSets = getSetsForParticipantInExercise(sessionId, exerciseId, participantId)
        remainingSets.forEachIndexed { index, set ->
            val expectedSetNumber = index + 1
            if (set.setNumber != expectedSetNumber) {
                updateSetLog(set.copy(setNumber = expectedSetNumber))
            }
        }
    }
}
