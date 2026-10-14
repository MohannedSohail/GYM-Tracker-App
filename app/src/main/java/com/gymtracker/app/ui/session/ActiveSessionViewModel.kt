package com.gymtracker.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveSessionUiState(
    val session: WorkoutSessionEntity? = null,
    val participants: List<ParticipantEntity> = emptyList(),
    val selectedParticipantId: Long? = null,
    val currentExercise: ExerciseEntity? = null,
    val weightInputKg: Double = 20.0,
    val repsInput: Int = 10,
    val loggedSets: List<SetLogEntity> = emptyList(),
    val previousPerformance: SetLogEntity? = null,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)

class ActiveSessionViewModel(
    private val repository: GymRepository,
    private val initialSessionId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    init {
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            repository.getActiveSession().collect { session ->
                _uiState.update { it.copy(session = session) }
                if (session != null) {
                    loadSessionParticipants(session.id)
                }
            }
        }
    }

    private fun loadSessionParticipants(sessionId: Long) {
        viewModelScope.launch {
            repository.getParticipantsForSession(sessionId).collect { participants ->
                _uiState.update { state ->
                    val defaultSelected = state.selectedParticipantId ?: participants.firstOrNull()?.id
                    state.copy(
                        participants = participants,
                        selectedParticipantId = defaultSelected
                    )
                }
                updatePreFillForCurrentSelection()
            }
        }
    }

    fun selectExercise(exercise: ExerciseEntity) {
        _uiState.update { it.copy(currentExercise = exercise) }
        observeSetsForCurrentExercise()
        updatePreFillForCurrentSelection()
    }

    fun selectParticipant(participantId: Long) {
        _uiState.update { it.copy(selectedParticipantId = participantId) }
        updatePreFillForCurrentSelection()
    }

    // AC-6: Fast Step Controls (+/- 2.5 kg for weight, +/- 1 for reps)
    fun onWeightIncrement(step: Double = 2.5) {
        _uiState.update { it.copy(weightInputKg = (it.weightInputKg + step).coerceAtLeast(0.5)) }
    }

    fun onWeightDecrement(step: Double = 2.5) {
        _uiState.update { it.copy(weightInputKg = (it.weightInputKg - step).coerceAtLeast(0.5)) }
    }

    // AC-7: Direct Numeric Keyboard Input for Decimal Weights
    fun onWeightDirectInput(newWeight: Double) {
        if (newWeight > 0.0) {
            _uiState.update { it.copy(weightInputKg = newWeight) }
        }
    }

    fun onRepsIncrement() {
        _uiState.update { it.copy(repsInput = (it.repsInput + 1).coerceAtLeast(1)) }
    }

    fun onRepsDecrement() {
        _uiState.update { it.copy(repsInput = (it.repsInput - 1).coerceAtLeast(1)) }
    }

    fun onRepsDirectInput(newReps: Int) {
        if (newReps >= 1) {
            _uiState.update { it.copy(repsInput = newReps) }
        }
    }

    // AC-8: Previous Set Pre-fill and Decrement Capability
    private fun updatePreFillForCurrentSelection() {
        val participantId = _uiState.value.selectedParticipantId ?: return
        val exerciseId = _uiState.value.currentExercise?.id ?: return

        viewModelScope.launch {
            val lastSet = repository.getLastRecordedRound(participantId, exerciseId)
            if (lastSet != null) {
                _uiState.update {
                    it.copy(
                        weightInputKg = lastSet.weightKg,
                        repsInput = lastSet.reps,
                        previousPerformance = lastSet
                    )
                }
            }
        }
    }

    private fun observeSetsForCurrentExercise() {
        val sessionId = _uiState.value.session?.id ?: return
        val exerciseId = _uiState.value.currentExercise?.id ?: return

        viewModelScope.launch {
            repository.getSetsForSessionAndExercise(sessionId, exerciseId).collect { sets ->
                _uiState.update { it.copy(loggedSets = sets) }
            }
        }
    }

    // AC-5: Log Round
    fun logRound() {
        val state = _uiState.value
        val sessionId = state.session?.id ?: return
        val exerciseId = state.currentExercise?.id ?: return
        val participantId = state.selectedParticipantId ?: return

        viewModelScope.launch {
            val result = repository.logRound(
                sessionId = sessionId,
                exerciseId = exerciseId,
                participantId = participantId,
                weightKg = state.weightInputKg,
                reps = state.repsInput
            )
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            } else {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    // AC-14: Delete Round
    fun deleteRound(setLogId: Long, participantId: Long) {
        val sessionId = _uiState.value.session?.id ?: return
        val exerciseId = _uiState.value.currentExercise?.id ?: return

        viewModelScope.launch {
            repository.deleteRound(
                setLogId = setLogId,
                sessionId = sessionId,
                exerciseId = exerciseId,
                participantId = participantId
            )
        }
    }

    // AC-10: Finish Session
    fun finishSession() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            repository.finishSession(sessionId)
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}
