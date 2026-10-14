package com.gymtracker.app.ui.session

import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.repository.FakeExerciseDao
import com.gymtracker.app.data.repository.FakeParticipantDao
import com.gymtracker.app.data.repository.FakeWorkoutDao
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.data.repository.GymRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeParticipantDao: FakeParticipantDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var repository: GymRepository
    private lateinit var viewModel: ActiveSessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeParticipantDao = FakeParticipantDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeWorkoutDao = FakeWorkoutDao()
        repository = GymRepositoryImpl(fakeParticipantDao, fakeExerciseDao, fakeWorkoutDao)
        viewModel = ActiveSessionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // AC-6: Fast Step Controls (+/- 2.5 kg for weight, +/- 1 for reps)
    @Test
    fun testFastStepControls_incrementsAndDecrementsCorrectly() {
        // Initial defaults: weight 20.0, reps 10
        assertEquals(20.0, viewModel.uiState.value.weightInputKg, 0.001)
        assertEquals(10, viewModel.uiState.value.repsInput)

        // Increment weight by 2.5
        viewModel.onWeightIncrement(2.5)
        assertEquals(22.5, viewModel.uiState.value.weightInputKg, 0.001)

        // Increment reps by 1
        viewModel.onRepsIncrement()
        assertEquals(11, viewModel.uiState.value.repsInput)

        // Decrement weight by 2.5
        viewModel.onWeightDecrement(2.5)
        assertEquals(20.0, viewModel.uiState.value.weightInputKg, 0.001)

        // Decrement reps by 1
        viewModel.onRepsDecrement()
        assertEquals(10, viewModel.uiState.value.repsInput)
    }

    // AC-7: Direct Numeric Keyboard Input for Decimal Weights
    @Test
    fun testDirectNumericKeyboardInput_updatesExactDecimalWeight() {
        viewModel.onWeightDirectInput(27.5)
        assertEquals(27.5, viewModel.uiState.value.weightInputKg, 0.001)

        viewModel.onRepsDirectInput(12)
        assertEquals(12, viewModel.uiState.value.repsInput)
    }

    // AC-8: Previous Set Pre-fill and Decrement Capability
    @Test
    fun testPreviousSetPreFill_loadsPreviousValuesAndAllowsDecrement() = runTest {
        val pId = repository.addParticipant("ماهر").getOrThrow()
        val exId = repository.addCustomExercise("سكوات بالبار", "الأرجل").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()

        // Log past round: 60.0 kg x 10 reps
        repository.logRound(sId, exId, pId, 60.0, 10)
        advanceUntilIdle()

        // Select exercise and participant in ViewModel
        val exercise = ExerciseEntity(id = exId, name = "سكوات بالبار", category = "الأرجل")
        viewModel.selectExercise(exercise)
        viewModel.selectParticipant(pId)
        advanceUntilIdle()

        // Must auto-prefill to 60.0 kg and 10 reps
        assertEquals(60.0, viewModel.uiState.value.weightInputKg, 0.001)
        assertEquals(10, viewModel.uiState.value.repsInput)

        // User can decrease pre-filled value
        viewModel.onWeightDecrement(2.5)
        assertEquals(57.5, viewModel.uiState.value.weightInputKg, 0.001)
    }

    // AC-15: Active Session Recovery After App Restart
    @Test
    fun testActiveSessionRecovery_restoresOngoingSessionState() = runTest {
        val pId = repository.addParticipant("عدنان").getOrThrow()
        val sId = repository.startSession(listOf(pId)).getOrThrow()
        advanceUntilIdle()

        // Simulate app restart by creating a fresh ViewModel instance
        val newViewModel = ActiveSessionViewModel(repository)
        advanceUntilIdle()

        assertNotNull(newViewModel.uiState.value.session)
        assertEquals(sId, newViewModel.uiState.value.session?.id)
    }
}
