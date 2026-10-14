package com.gymtracker.app.data.repository

import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    // Participants
    fun getActiveParticipants(): Flow<List<ParticipantEntity>>
    fun getAllParticipants(): Flow<List<ParticipantEntity>>
    suspend fun addParticipant(name: String): Result<Long>
    suspend fun renameParticipant(id: Long, newName: String): Result<Unit>
    suspend fun deleteParticipant(id: Long): Result<Unit>

    // Exercises
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>
    suspend fun addCustomExercise(name: String, category: String): Result<Long>

    // Sessions
    fun getActiveSession(): Flow<WorkoutSessionEntity?>
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>
    fun getParticipantsForSession(sessionId: Long): Flow<List<ParticipantEntity>>
    suspend fun startSession(participantIds: List<Long>): Result<Long>
    suspend fun finishSession(sessionId: Long): Result<Unit>

    // Rounds / Set Logs
    fun getSetsForSessionAndExercise(sessionId: Long, exerciseId: Long): Flow<List<SetLogEntity>>
    fun getSetsForSession(sessionId: Long): Flow<List<SetLogEntity>>
    suspend fun getLastRecordedRound(participantId: Long, exerciseId: Long): SetLogEntity?
    suspend fun logRound(
        sessionId: Long,
        exerciseId: Long,
        participantId: Long,
        weightKg: Double,
        reps: Int
    ): Result<Long>
    suspend fun updateRound(setLogId: Long, newWeightKg: Double, newReps: Int): Result<Unit>
    suspend fun deleteRound(
        setLogId: Long,
        sessionId: Long,
        exerciseId: Long,
        participantId: Long
    ): Result<Unit>
}
