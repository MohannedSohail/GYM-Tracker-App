package com.gymtracker.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ExerciseDao.
 *
 * Covers:
 *   AC-3 — Custom exercise saved with isCustom = true and correct category
 */
@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

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

    // AC-3: Custom exercise is saved with isCustom = true
    @Test
    fun testCustomExerciseCreation_savesWithIsCustomTrue() = runTest {
        val dao = db.exerciseDao()

        dao.insertExercise(ExerciseEntity(name = "سحب ظهر واسع", category = "الظهر", isCustom = true))

        val all = dao.getAllExercises().first()
        val custom = all.find { it.name == "سحب ظهر واسع" }

        assertNotNull(custom)
        assertTrue(custom!!.isCustom)
        assertEquals("الظهر", custom.category)
    }

    // Filter by category returns only matching exercises
    @Test
    fun testGetByCategory_returnsOnlyCategoryExercises() = runTest {
        val dao = db.exerciseDao()

        dao.insertExercise(ExerciseEntity(name = "بنش برس", category = "الصدر"))
        dao.insertExercise(ExerciseEntity(name = "دمبل صدر", category = "الصدر"))
        dao.insertExercise(ExerciseEntity(name = "سكوات", category = "الأرجل"))

        val chest = dao.getExercisesByCategory("الصدر").first()

        assertEquals(2, chest.size)
        assertTrue(chest.all { it.category == "الصدر" })
    }

    // countExercises returns exact count after insertAll
    @Test
    fun testInsertAll_countsCorrectly() = runTest {
        val dao = db.exerciseDao()

        val list = listOf(
            ExerciseEntity(name = "تمرين 1", category = "الصدر"),
            ExerciseEntity(name = "تمرين 2", category = "الظهر"),
            ExerciseEntity(name = "تمرين 3", category = "الأكتاف")
        )
        dao.insertAll(list)

        assertEquals(3, dao.countExercises())
    }

    // getExerciseById returns the correct entity
    @Test
    fun testGetById_returnsCorrectExercise() = runTest {
        val dao = db.exerciseDao()

        val id = dao.insertExercise(ExerciseEntity(name = "ديدليفت", category = "الظهر"))
        val fetched = dao.getExerciseById(id)

        assertNotNull(fetched)
        assertEquals("ديدليفت", fetched!!.name)
        assertEquals("الظهر", fetched.category)
    }
}
