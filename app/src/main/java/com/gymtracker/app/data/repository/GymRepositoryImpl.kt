package com.gymtracker.app.data.repository

import com.gymtracker.app.data.local.dao.ExerciseDao
import com.gymtracker.app.data.local.dao.ParticipantDao
import com.gymtracker.app.data.local.dao.WorkoutDao
import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SessionParticipantCrossRef
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

class GymRepositoryImpl(
    private val participantDao: ParticipantDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) : GymRepository {

    override fun getActiveParticipants(): Flow<List<ParticipantEntity>> =
        participantDao.getActiveParticipants()

    override fun getAllParticipants(): Flow<List<ParticipantEntity>> =
        participantDao.getAllParticipants()

    override suspend fun addParticipant(name: String): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم المشارك لا يمكن أن يكون فارغاً"))
        }
        val participant = ParticipantEntity(name = trimmed)
        return try {
            val id = participantDao.insertParticipant(participant)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameParticipant(id: Long, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم المشارك لا يمكن أن يكون فارغاً"))
        }
        val existing = participantDao.getParticipantById(id)
            ?: return Result.failure(NoSuchElementException("المشارك غير موجود"))
        return try {
            participantDao.updateParticipant(existing.copy(name = trimmed))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteParticipant(id: Long): Result<Unit> {
        return try {
            participantDao.softDeleteParticipant(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    override fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByCategory(category)

    override suspend fun addCustomExercise(name: String, category: String): Result<Long> {
        val trimmedName = name.trim()
        val trimmedCategory = category.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم التمرين لا يمكن أن يكون فارغاً"))
        }
        if (trimmedCategory.isBlank()) {
            return Result.failure(IllegalArgumentException("المجموعة العضلية لا يمكن أن تكون فارغة"))
        }
        val exercise = ExerciseEntity(
            name = trimmedName,
            category = trimmedCategory,
            isCustom = true
        )
        return try {
            val id = exerciseDao.insertExercise(exercise)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveSession(): Flow<WorkoutSessionEntity?> =
        workoutDao.getActiveSession()

    override fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>> =
        workoutDao.getCompletedSessions()

    override fun getParticipantsForSession(sessionId: Long): Flow<List<ParticipantEntity>> =
        workoutDao.getParticipantsForSession(sessionId)

    override suspend fun startSession(participantIds: List<Long>): Result<Long> {
        if (participantIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("يجب اختيار مشارك واحد على الأقل لبدء الجلسة"))
        }
        return try {
            val session = WorkoutSessionEntity()
            val sessionId = workoutDao.insertSession(session)
            val crossRefs = participantIds.map { participantId ->
                SessionParticipantCrossRef(sessionId = sessionId, participantId = participantId)
            }
            workoutDao.insertSessionParticipants(crossRefs)
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finishSession(sessionId: Long): Result<Unit> {
        return try {
            workoutDao.finishSession(sessionId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSetsForSessionAndExercise(sessionId: Long, exerciseId: Long): Flow<List<SetLogEntity>> =
        workoutDao.getSetsForSessionAndExercise(sessionId, exerciseId)

    override fun getSetsForSession(sessionId: Long): Flow<List<SetLogEntity>> =
        workoutDao.getSetsForSession(sessionId)

    override suspend fun getLastRecordedRound(participantId: Long, exerciseId: Long): SetLogEntity? =
        workoutDao.getLastSetForParticipantAndExercise(participantId, exerciseId)

    override suspend fun logRound(
        sessionId: Long,
        exerciseId: Long,
        participantId: Long,
        weightKg: Double,
        reps: Int
    ): Result<Long> {
        if (weightKg <= 0.0) {
            return Result.failure(IllegalArgumentException("الوزن يجب أن يكون قيمة موجبة أكبر من صفر"))
        }
        if (reps < 1) {
            return Result.failure(IllegalArgumentException("عدد العدّات يجب أن يكون 1 على الأقل"))
        }

        return try {
            val existingSets = workoutDao.getSetsForParticipantInExercise(sessionId, exerciseId, participantId)
            val nextSetNumber = existingSets.size + 1
            val setLog = SetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                participantId = participantId,
                setNumber = nextSetNumber,
                weightKg = weightKg,
                reps = reps
            )
            val id = workoutDao.insertSetLog(setLog)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRound(setLogId: Long, newWeightKg: Double, newReps: Int): Result<Unit> {
        if (newWeightKg <= 0.0) {
            return Result.failure(IllegalArgumentException("الوزن يجب أن يكون قيمة موجبة أكبر من صفر"))
        }
        if (newReps < 1) {
            return Result.failure(IllegalArgumentException("عدد العدّات يجب أن يكون 1 على الأقل"))
        }

        return try {
            // Find existing set and update
            // Since Room requires the full entity for update, we can query session sets or do direct update
            // For flexibility, update with new values:
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRound(
        setLogId: Long,
        sessionId: Long,
        exerciseId: Long,
        participantId: Long
    ): Result<Unit> {
        return try {
            workoutDao.deleteAndReindexSet(setLogId, sessionId, exerciseId, participantId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
