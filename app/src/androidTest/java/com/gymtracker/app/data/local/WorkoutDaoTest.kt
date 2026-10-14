package com.gymtracker.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SessionParticipantCrossRef
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for WorkoutDao.
 *
 * Covers:
 *   AC-4  — Session created with selected participant cross-refs
 *   AC-5  — Set log stores exact weightKg (decimal) and reps
 *   AC-9  — Session completion updates endTime
 *   AC-13 — Set deletion re-indexes remaining sets
 */
@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: AppDatabase

    // Shared test fixtures — inserted once per test in setUp helpers
    private var participantId: Long = 0
    private var exerciseId: Long = 0

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    /** Inserts one participant and one exercise, stores their IDs. */
    private suspend fun insertFixtures() {
        participantId = db.participantDao()
            .insertParticipant(ParticipantEntity(name = "أحمد"))
        exerciseId = db.exerciseDao()
            .insertExercise(ExerciseEntity(name = "بنش برس", category = "الصدر"))
    }

    /** Creates a new workout session and links it to [participantId]. */
    private suspend fun startSession(): Long {
        val sessionId = db.workoutDao()
            .insertSession(WorkoutSessionEntity())
        db.workoutDao().insertSessionParticipants(
            listOf(SessionParticipantCrossRef(sessionId, participantId))
        )
        return sessionId
    }

    // AC-4: Session is created with the correct participant cross-refs
    @Test
    fun testWorkoutSessionInit_createsSessionWithSelectedParticipants() = runTest {
        insertFixtures()

        val p2Id = db.participantDao()
            .insertParticipant(ParticipantEntity(name = "محمد"))

        val sessionId = db.workoutDao().insertSession(WorkoutSessionEntity())
        db.workoutDao().insertSessionParticipants(
            listOf(
                SessionParticipantCrossRef(sessionId, participantId),
                SessionParticipantCrossRef(sessionId, p2Id)
            )
        )

        val participants = db.workoutDao().getParticipantsForSession(sessionId).first()

        assertEquals(2, participants.size)
        val ids = participants.map { it.id }
        assert(participantId in ids)
        assert(p2Id in ids)
    }

    // AC-5: Set log stores exact weightKg (including decimal) and reps
    @Test
    fun testSetLogging_savesSetLogWithKgAndReps() = runTest {
        insertFixtures()
        val sessionId = startSession()

        db.workoutDao().insertSetLog(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                participantId = participantId,
                setNumber = 1,
                weightKg = 22.5,
                reps = 10
            )
        )

        val sets = db.workoutDao()
            .getSetsForSessionAndExercise(sessionId, exerciseId).first()

        assertEquals(1, sets.size)
        assertEquals(22.5, sets[0].weightKg, 0.001)
        assertEquals(10, sets[0].reps)
        assertEquals(1, sets[0].setNumber)
    }

    // AC-9: finishSession updates endTime; active session becomes null
    @Test
    fun testSessionCompletion_updatesEndTime() = runTest {
        insertFixtures()
        val sessionId = startSession()

        // Before finish — session has no endTime
        val active = db.workoutDao().getActiveSession().first()
        assertNotNull(active)
        assertNull(active!!.endTime)

        val finishTime = System.currentTimeMillis()
        db.workoutDao().finishSession(sessionId, finishTime)

        // After finish — active session must be null
        val noActive = db.workoutDao().getActiveSession().first()
        assertNull(noActive)

        // Completed sessions list must contain the session with an endTime
        val completed = db.workoutDao().getCompletedSessions().first()
        assertEquals(1, completed.size)
        assertNotNull(completed[0].endTime)
    }

    // AC-13: deleteAndReindexSet removes the set and re-numbers remaining sets
    @Test
    fun testSetModificationAndDeletion_updatesDbAndReindexes() = runTest {
        insertFixtures()
        val sessionId = startSession()

        val dao = db.workoutDao()

        val id1 = dao.insertSetLog(SetLogEntity(sessionId = sessionId, exerciseId = exerciseId, participantId = participantId, setNumber = 1, weightKg = 30.0, reps = 10))
        val id2 = dao.insertSetLog(SetLogEntity(sessionId = sessionId, exerciseId = exerciseId, participantId = participantId, setNumber = 2, weightKg = 32.5, reps = 8))
        val id3 = dao.insertSetLog(SetLogEntity(sessionId = sessionId, exerciseId = exerciseId, participantId = participantId, setNumber = 3, weightKg = 35.0, reps = 6))

        // Delete middle set and reindex
        dao.deleteAndReindexSet(id2, sessionId, exerciseId, participantId)

        val remaining = dao.getSetsForSessionAndExercise(sessionId, exerciseId).first()

        assertEquals(2, remaining.size)
        assertEquals(1, remaining[0].setNumber)
        assertEquals(30.0, remaining[0].weightKg, 0.001)
        assertEquals(2, remaining[1].setNumber)   // re-indexed from 3 → 2
        assertEquals(35.0, remaining[1].weightKg, 0.001)
    }

    // AC-7: getLastSetForParticipantAndExercise returns the most-recent set
    @Test
    fun testLastSet_returnsLastRecordedSetForParticipantAndExercise() = runTest {
        insertFixtures()
        val sessionId = startSession()
        val dao = db.workoutDao()

        dao.insertSetLog(SetLogEntity(sessionId = sessionId, exerciseId = exerciseId, participantId = participantId, setNumber = 1, weightKg = 40.0, reps = 8, loggedAt = 1000L))
        dao.insertSetLog(SetLogEntity(sessionId = sessionId, exerciseId = exerciseId, participantId = participantId, setNumber = 2, weightKg = 42.5, reps = 6, loggedAt = 2000L))

        val last = dao.getLastSetForParticipantAndExercise(participantId, exerciseId)

        assertNotNull(last)
        assertEquals(42.5, last!!.weightKg, 0.001)
        assertEquals(2000L, last.loggedAt)
    }
}
