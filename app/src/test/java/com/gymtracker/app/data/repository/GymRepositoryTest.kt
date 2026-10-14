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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GymRepositoryTest {

    private lateinit var fakeParticipantDao: FakeParticipantDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var repository: GymRepository

    @Before
    fun setUp() {
        fakeParticipantDao = FakeParticipantDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeWorkoutDao = FakeWorkoutDao()
        repository = GymRepositoryImpl(fakeParticipantDao, fakeExerciseDao, fakeWorkoutDao)
    }

    // AC-1: Initial Participant Setup
    @Test
    fun testInitialParticipantSetup_savesActiveParticipants() = runTest {
        val resultA = repository.addParticipant("أحمد")
        val resultB = repository.addParticipant("محمد")

        assertTrue(resultA.isSuccess)
        assertTrue(resultB.isSuccess)

        val activeList = repository.getActiveParticipants().first()
        assertEquals(2, activeList.size)
        assertTrue(activeList.all { it.isActive })
        assertEquals("أحمد", activeList[0].name)
        assertEquals("محمد", activeList[1].name)
    }

    // AC-12: Input Validation for Names (Reject blank/spaces)
    @Test
    fun testInputValidation_rejectsBlankParticipantAndExerciseNames() = runTest {
        val blankNameResult = repository.addParticipant("   ")
        assertTrue(blankNameResult.isFailure)

        val blankExerciseResult = repository.addCustomExercise("  ", "الصدر")
        assertTrue(blankExerciseResult.isFailure)

        val blankCategoryResult = repository.addCustomExercise("تمرين جديد", "   ")
        assertTrue(blankCategoryResult.isFailure)
    }

    // AC-3: Custom Exercise Creation
    @Test
    fun testCustomExerciseCreation_savesWithIsCustomTrue() = runTest {
        val result = repository.addCustomExercise("سحب ظهر واسع", "الظهر")
        assertTrue(result.isSuccess)

        val exercises = repository.getAllExercises().first()
        val created = exercises.find { it.name == "سحب ظهر واسع" }
        assertNotNull(created)
        assertTrue(created!!.isCustom)
        assertEquals("الظهر", created.category)
    }

    // AC-4: Workout Session Initialization with Selected Participants
    @Test
    fun testWorkoutSessionInit_createsSessionWithSelectedParticipants() = runTest {
        val p1 = repository.addParticipant("علي").getOrThrow()
        val p2 = repository.addParticipant("عمر").getOrThrow()
        val p3 = repository.addParticipant("يوسف").getOrThrow()

        // Start session with only p1 and p2
        val sessionResult = repository.startSession(listOf(p1, p2))
        assertTrue(sessionResult.isSuccess)
        val sessionId = sessionResult.getOrThrow()

        val activeSession = repository.getActiveSession().first()
        assertNotNull(activeSession)
        assertEquals(sessionId, activeSession?.id)

        val participantsInSession = repository.getParticipantsForSession(sessionId).first()
        assertEquals(2, participantsInSession.size)
        assertTrue(participantsInSession.any { it.id == p1 })
        assertTrue(participantsInSession.any { it.id == p2 })
        assertFalse(participantsInSession.any { it.id == p3 })
    }

    // AC-5: Round Logging with Decimal KG & Reps
    @Test
    fun testRoundLogging_savesDecimalKgAndReps() = runTest {
        val pId = repository.addParticipant("سامي").getOrThrow()
        val exId = repository.addCustomExercise("بنش برس مستوي", "الصدر").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()

        val logResult = repository.logRound(
            sessionId = sId,
            exerciseId = exId,
            participantId = pId,
            weightKg = 22.5,
            reps = 10
        )
        assertTrue(logResult.isSuccess)

        val sets = repository.getSetsForSessionAndExercise(sId, exId).first()
        assertEquals(1, sets.size)
        assertEquals(22.5, sets[0].weightKg, 0.001)
        assertEquals(10, sets[0].reps)
        assertEquals(1, sets[0].setNumber)
    }

    // AC-11: Input Validation for Weight and Reps
    @Test
    fun testInputValidation_rejectsNonPositiveWeightsAndReps() = runTest {
        val pId = repository.addParticipant("طارق").getOrThrow()
        val exId = repository.addCustomExercise("سكوات", "الأرجل").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()

        // Negative or zero weight must fail
        val zeroWeight = repository.logRound(sId, exId, pId, 0.0, 10)
        assertTrue(zeroWeight.isFailure)

        val negWeight = repository.logRound(sId, exId, pId, -5.0, 10)
        assertTrue(negWeight.isFailure)

        // Non-positive reps must fail
        val zeroReps = repository.logRound(sId, exId, pId, 50.0, 0)
        assertTrue(zeroReps.isFailure)

        val negReps = repository.logRound(sId, exId, pId, 50.0, -1)
        assertTrue(negReps.isFailure)
    }

    // AC-13: Soft Deletion of Participant with Existing History
    @Test
    fun testSoftDelete_setsIsActiveFalseAndPreservesSetLogs() = runTest {
        val pId = repository.addParticipant("خالد").getOrThrow()
        val exId = repository.addCustomExercise("ديدليفت", "الظهر").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()
        repository.logRound(sId, exId, pId, 80.0, 8)

        // Soft delete Khalid
        val deleteResult = repository.deleteParticipant(pId)
        assertTrue(deleteResult.isSuccess)

        // Khalid must disappear from active participants
        val activeList = repository.getActiveParticipants().first()
        assertFalse(activeList.any { it.id == pId })

        // But remain in all participants (soft deleted)
        val allList = repository.getAllParticipants().first()
        val deletedUser = allList.find { it.id == pId }
        assertNotNull(deletedUser)
        assertFalse(deletedUser!!.isActive)

        // And historical sets remain completely intact
        val sessionSets = repository.getSetsForSession(sId).first()
        assertEquals(1, sessionSets.size)
        assertEquals(80.0, sessionSets[0].weightKg, 0.001)
    }

    // AC-10: Session Completion (Updates endTime)
    @Test
    fun testSessionCompletion_updatesEndTime() = runTest {
        val pId = repository.addParticipant("حسين").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()

        val finishResult = repository.finishSession(sId)
        assertTrue(finishResult.isSuccess)

        // Active session should now be null
        val activeSession = repository.getActiveSession().first()
        assertEquals(null, activeSession)

        // Completed sessions should contain this session
        val completed = repository.getCompletedSessions().first()
        assertEquals(1, completed.size)
        assertNotNull(completed[0].endTime)
    }

    // AC-14: In-Session Round Deletion & Re-indexing
    @Test
    fun testRoundDeletion_reindexesRemainingRounds() = runTest {
        val pId = repository.addParticipant("ماجد").getOrThrow()
        val exId = repository.addCustomExercise("ضغط أكتاف", "الأكتاف").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()

        val id1 = repository.logRound(sId, exId, pId, 30.0, 10).getOrThrow()
        val id2 = repository.logRound(sId, exId, pId, 32.5, 8).getOrThrow()
        val id3 = repository.logRound(sId, exId, pId, 35.0, 6).getOrThrow()

        // Delete middle round (round 2)
        val deleteResult = repository.deleteRound(id2, sId, exId, pId)
        assertTrue(deleteResult.isSuccess)

        val sets = repository.getSetsForSessionAndExercise(sId, exId).first()
        assertEquals(2, sets.size)
        assertEquals(1, sets[0].setNumber)
        assertEquals(30.0, sets[0].weightKg, 0.001)
        assertEquals(2, sets[1].setNumber) // Reindexed to 2!
        assertEquals(35.0, sets[1].weightKg, 0.001)
    }
}

// --- Fake DAOs for In-Memory Unit Testing ---

class FakeParticipantDao : ParticipantDao {
    private val participants = mutableListOf<ParticipantEntity>()
    private val flow = MutableStateFlow<List<ParticipantEntity>>(emptyList())
    private var nextId = 1L

    override fun getActiveParticipants(): Flow<List<ParticipantEntity>> =
        flow.map { it.filter { p -> p.isActive } }

    override fun getAllParticipants(): Flow<List<ParticipantEntity>> = flow

    override suspend fun getParticipantById(id: Long): ParticipantEntity? =
        participants.find { it.id == id }

    override suspend fun insertParticipant(participant: ParticipantEntity): Long {
        val id = nextId++
        participants.add(participant.copy(id = id))
        flow.value = participants.toList()
        return id
    }

    override suspend fun updateParticipant(participant: ParticipantEntity) {
        val index = participants.indexOfFirst { it.id == participant.id }
        if (index != -1) {
            participants[index] = participant
            flow.value = participants.toList()
        }
    }

    override suspend fun softDeleteParticipant(id: Long) {
        val existing = getParticipantById(id)
        if (existing != null) {
            updateParticipant(existing.copy(isActive = false))
        }
    }
}

class FakeExerciseDao : ExerciseDao {
    private val exercises = mutableListOf<ExerciseEntity>()
    private val flow = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllExercises(): Flow<List<ExerciseEntity>> = flow

    override fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>> =
        flow.map { it.filter { e -> e.category == category } }

    override suspend fun getExerciseById(id: Long): ExerciseEntity? =
        exercises.find { it.id == id }

    override suspend fun countExercises(): Int = exercises.size

    override suspend fun insertExercise(exercise: ExerciseEntity): Long {
        val id = nextId++
        exercises.add(exercise.copy(id = id))
        flow.value = exercises.toList()
        return id
    }

    override suspend fun insertAll(exercisesList: List<ExerciseEntity>) {
        exercisesList.forEach { insertExercise(it) }
    }
}

class FakeWorkoutDao : WorkoutDao {
    private val sessions = mutableListOf<WorkoutSessionEntity>()
    private val sessionFlow = MutableStateFlow<List<WorkoutSessionEntity>>(emptyList())
    private val crossRefs = mutableListOf<SessionParticipantCrossRef>()
    private val sets = mutableListOf<SetLogEntity>()
    private val setsFlow = MutableStateFlow<List<SetLogEntity>>(emptyList())
    private var nextSessionId = 1L
    private var nextSetId = 1L

    override fun getActiveSession(): Flow<WorkoutSessionEntity?> =
        sessionFlow.map { it.firstOrNull { s -> s.endTime == null } }

    override suspend fun getSessionById(id: Long): WorkoutSessionEntity? =
        sessions.find { it.id == id }

    override fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>> =
        sessionFlow.map { it.filter { s -> s.endTime != null } }

    override suspend fun insertSession(session: WorkoutSessionEntity): Long {
        val id = nextSessionId++
        sessions.add(session.copy(id = id))
        sessionFlow.value = sessions.toList()
        return id
    }

    override suspend fun finishSession(sessionId: Long, endTime: Long) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index != -1) {
            sessions[index] = sessions[index].copy(endTime = endTime)
            sessionFlow.value = sessions.toList()
        }
    }

    override suspend fun insertSessionParticipants(crossRefsList: List<SessionParticipantCrossRef>) {
        crossRefs.addAll(crossRefsList)
    }

    override fun getParticipantsForSession(sessionId: Long): Flow<List<ParticipantEntity>> {
        val pIds = crossRefs.filter { it.sessionId == sessionId }.map { it.participantId }
        return MutableStateFlow(pIds.map { ParticipantEntity(id = it, name = "User$it") })
    }

    override suspend fun getParticipantIdsForSession(sessionId: Long): List<Long> =
        crossRefs.filter { it.sessionId == sessionId }.map { it.participantId }

    override suspend fun insertSetLog(setLog: SetLogEntity): Long {
        val id = nextSetId++
        sets.add(setLog.copy(id = id))
        setsFlow.value = sets.toList()
        return id
    }

    override suspend fun updateSetLog(setLog: SetLogEntity) {
        val index = sets.indexOfFirst { it.id == setLog.id }
        if (index != -1) {
            sets[index] = setLog
            setsFlow.value = sets.toList()
        }
    }

    override suspend fun deleteSetLog(setLogId: Long) {
        sets.removeAll { it.id == setLogId }
        setsFlow.value = sets.toList()
    }

    override fun getSetsForSessionAndExercise(sessionId: Long, exerciseId: Long): Flow<List<SetLogEntity>> =
        setsFlow.map { it.filter { s -> s.sessionId == sessionId && s.exerciseId == exerciseId } }

    override fun getSetsForSession(sessionId: Long): Flow<List<SetLogEntity>> =
        setsFlow.map { it.filter { s -> s.sessionId == sessionId } }

    override suspend fun getLastSetForParticipantAndExercise(participantId: Long, exerciseId: Long): SetLogEntity? =
        sets.filter { it.participantId == participantId && it.exerciseId == exerciseId }.maxByOrNull { it.loggedAt }

    override suspend fun getSetsForParticipantInExercise(
        sessionId: Long,
        exerciseId: Long,
        participantId: Long
    ): List<SetLogEntity> =
        sets.filter { it.sessionId == sessionId && it.exerciseId == exerciseId && it.participantId == participantId }
            .sortedBy { it.setNumber }
}
