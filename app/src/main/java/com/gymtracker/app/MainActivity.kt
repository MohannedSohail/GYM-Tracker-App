package com.gymtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gymtracker.app.data.local.AppDatabase
import com.gymtracker.app.data.repository.GymRepositoryImpl
import com.gymtracker.app.ui.exercises.ExercisesViewModel
import com.gymtracker.app.ui.history.HistoryViewModel
import com.gymtracker.app.ui.navigation.GymNavGraph
import com.gymtracker.app.ui.participants.ParticipantsViewModel
import com.gymtracker.app.ui.session.ActiveSessionViewModel
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraGymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = GymRepositoryImpl(
            participantDao = database.participantDao(),
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao()
        )

        val participantsViewModel = ParticipantsViewModel(repository)
        val exercisesViewModel = ExercisesViewModel(repository)
        val activeSessionViewModel = ActiveSessionViewModel(repository)
        val historyViewModel = HistoryViewModel(repository)

        setContent {
            TerraGymTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TerraBackground
                ) {
                    val navController = rememberNavController()
                    GymNavGraph(
                        navController = navController,
                        repository = repository,
                        participantsViewModel = participantsViewModel,
                        exercisesViewModel = exercisesViewModel,
                        activeSessionViewModel = activeSessionViewModel,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }
}
