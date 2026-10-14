package com.gymtracker.app.ui.participants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParticipantsUiState(
    val selectedParticipantForEdit: ParticipantEntity? = null,
    val selectedParticipantForDelete: ParticipantEntity? = null,
    val errorMessage: String? = null
)

class ParticipantsViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val activeParticipants: StateFlow<List<ParticipantEntity>> =
        repository.getActiveParticipants()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParticipants: StateFlow<List<ParticipantEntity>> =
        repository.getAllParticipants()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ParticipantsUiState())
    val uiState: StateFlow<ParticipantsUiState> = _uiState.asStateFlow()

    fun addParticipant(name: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.addParticipant(name)
            if (result.isSuccess) {
                _uiState.update { it.copy(errorMessage = null) }
                onComplete()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun renameParticipant(id: Long, newName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.renameParticipant(id, newName)
            if (result.isSuccess) {
                _uiState.update { it.copy(selectedParticipantForEdit = null, errorMessage = null) }
                onComplete()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun deleteParticipant(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteParticipant(id)
            if (result.isSuccess) {
                _uiState.update { it.copy(selectedParticipantForDelete = null, errorMessage = null) }
                onComplete()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun selectForEdit(participant: ParticipantEntity?) {
        _uiState.update { it.copy(selectedParticipantForEdit = participant) }
    }

    fun selectForDelete(participant: ParticipantEntity?) {
        _uiState.update { it.copy(selectedParticipantForDelete = participant) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
