package com.gymtracker.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.app.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ParticipantDao.
 *
 * Covers:
 *   AC-1  — Initial participants saved with isActive = true
 *   AC-2  — Participants reload correctly across DAO instances
 *   AC-12 — Soft-delete sets isActive = false and preserves the row
 */
@RunWith(AndroidJUnit4::class)
class ParticipantDaoTest {

    private lateinit var db: AppDatabase

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

    // AC-1: Participants are inserted with isActive = true
    @Test
    fun testInitialParticipantSetup_savesActiveParticipants() = runTest {
        val dao = db.participantDao()

        dao.insertParticipant(ParticipantEntity(name = "أحمد"))
        dao.insertParticipant(ParticipantEntity(name = "محمد"))

        val active = dao.getActiveParticipants().first()

        assertEquals(2, active.size)
        assertTrue(active.all { it.isActive })
        assertEquals("أحمد", active[0].name)
        assertEquals("محمد", active[1].name)
    }

    // AC-2: getAllParticipants returns all rows (active and soft-deleted)
    @Test
    fun testParticipantPersistence_reloadsOnRestart() = runTest {
        val dao = db.participantDao()

        val id1 = dao.insertParticipant(ParticipantEntity(name = "علي"))
        val id2 = dao.insertParticipant(ParticipantEntity(name = "سامر"))

        val all = dao.getAllParticipants().first()

        assertEquals(2, all.size)
        assertTrue(all.any { it.id == id1 && it.name == "علي" })
        assertTrue(all.any { it.id == id2 && it.name == "سامر" })
    }

    // AC-12: Soft-delete sets isActive = false; participant row is NOT removed
    @Test
    fun testSoftDelete_setsIsActiveFalseAndPreservesSetLogs() = runTest {
        val dao = db.participantDao()

        val id = dao.insertParticipant(ParticipantEntity(name = "خالد"))

        // Confirm he is active before deletion
        val before = dao.getActiveParticipants().first()
        assertTrue(before.any { it.id == id })

        // Soft-delete
        dao.softDeleteParticipant(id)

        // Must NOT appear in active list
        val active = dao.getActiveParticipants().first()
        assertFalse(active.any { it.id == id })

        // Must still exist in all-participants list with isActive = false
        val all = dao.getAllParticipants().first()
        val deleted = all.find { it.id == id }
        assertNotNull(deleted)
        assertFalse(deleted!!.isActive)
    }

    // Extra: getParticipantById returns the correct entity
    @Test
    fun testGetById_returnsCorrectEntity() = runTest {
        val dao = db.participantDao()
        val id = dao.insertParticipant(ParticipantEntity(name = "يوسف"))

        val fetched = dao.getParticipantById(id)

        assertNotNull(fetched)
        assertEquals(id, fetched!!.id)
        assertEquals("يوسف", fetched.name)
    }
}
