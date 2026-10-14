package com.gymtracker.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.SetLogEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: WorkoutSessionEntity? = null,
    val participants: List<ParticipantEntity> = emptyList(),
    val sets: List<SetLogEntity> = emptyList(),
    val exercisesMap: Map<Long, ExerciseEntity> = emptyMap()
)

class HistoryViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val completedSessions: StateFlow<List<WorkoutSessionEntity>> =
        repository.getCompletedSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionDetail = MutableStateFlow<SessionDetailUiState?>(null)
    val selectedSessionDetail: StateFlow<SessionDetailUiState?> = _selectedSessionDetail.asStateFlow()

    fun loadSessionDetails(session: WorkoutSessionEntity) {
        viewModelScope.launch {
            val participants = repository.getParticipantsForSession(session.id).first()
            val sets = repository.getSetsForSession(session.id).first()
            val exercises = repository.getAllExercises().first().associateBy { it.id }

            _selectedSessionDetail.value = SessionDetailUiState(
                session = session,
                participants = participants,
                sets = sets,
                exercisesMap = exercises
            )
        }
    }

    fun clearSelectedSession() {
        _selectedSessionDetail.value = null
    }
}
