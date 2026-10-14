package com.gymtracker.app.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.local.entity.ExerciseEntity
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val MuscleCategories = listOf(
    "الكل",
    "الصدر",
    "الظهر",
    "الأرجل",
    "الأكتاف",
    "البايسبس",
    "الترايسبس",
    "السواعد",
    "الترابيس",
    "البطن"
)

data class ExercisesUiState(
    val selectedCategory: String = "الكل",
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class ExercisesViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisesUiState())
    val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()

    private val allExercises = repository.getAllExercises()

    val filteredExercises: StateFlow<List<ExerciseEntity>> =
        combine(allExercises, _uiState) { exercises, state ->
            exercises.filter { exercise ->
                val matchesCategory = state.selectedCategory == "الكل" || exercise.category == state.selectedCategory
                val matchesSearch = state.searchQuery.isBlank() || exercise.name.contains(state.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addCustomExercise(name: String, category: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.addCustomExercise(name, category)
            if (result.isSuccess) {
                _uiState.update { it.copy(errorMessage = null) }
                onComplete()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }
}
