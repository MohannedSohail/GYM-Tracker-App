package com.gymtracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gymtracker.app.data.local.DefaultExercises
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.ui.exercises.ExercisePickerScreen
import com.gymtracker.app.ui.exercises.ExercisesViewModel
import com.gymtracker.app.ui.history.HistoryScreen
import com.gymtracker.app.ui.history.HistoryViewModel
import com.gymtracker.app.ui.home.HomeScreen
import com.gymtracker.app.ui.participants.FirstRunSetupScreen
import com.gymtracker.app.ui.participants.FriendsScreen
import com.gymtracker.app.ui.participants.ParticipantsViewModel
import com.gymtracker.app.ui.session.ActiveSessionScreen
import com.gymtracker.app.ui.session.ActiveSessionViewModel
import com.gymtracker.app.ui.splash.SplashScreen
import kotlinx.coroutines.launch

object Destinations {
    const val SPLASH = "splash"
    const val FIRST_RUN = "first_run"
    const val HOME = "home"
    const val ACTIVE_SESSION = "active_session"
    const val EXERCISE_PICKER = "exercise_picker"
    const val FRIENDS = "friends"
    const val HISTORY = "history"
}

@Composable
fun GymNavGraph(
    navController: NavHostController,
    repository: GymRepository,
    participantsViewModel: ParticipantsViewModel,
    exercisesViewModel: ExercisesViewModel,
    activeSessionViewModel: ActiveSessionViewModel,
    historyViewModel: HistoryViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val activeParticipants by participantsViewModel.activeParticipants.collectAsState()
    val activeSession by repository.getActiveSession().collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        // 01 Splash
        composable(Destinations.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    if (activeParticipants.isEmpty()) {
                        navController.navigate(Destinations.FIRST_RUN) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    } else if (activeSession != null) {
                        navController.navigate(Destinations.ACTIVE_SESSION) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 02 FirstRunSetup
        composable(Destinations.FIRST_RUN) {
            FirstRunSetupScreen(
                onSetupComplete = { names ->
                    coroutineScope.launch {
                        names.forEach { repository.addParticipant(it) }
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.FIRST_RUN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 03 Home
        composable(Destinations.HOME) {
            HomeScreen(
                activeParticipants = activeParticipants,
                activeSession = activeSession,
                onStartSession = { selectedIds ->
                    coroutineScope.launch {
                        val result = repository.startSession(selectedIds)
                        if (result.isSuccess) {
                            activeSessionViewModel.selectExercise(DefaultExercises.list.first())
                            navController.navigate(Destinations.ACTIVE_SESSION)
                        }
                    }
                },
                onResumeSession = {
                    activeSessionViewModel.selectExercise(DefaultExercises.list.first())
                    navController.navigate(Destinations.ACTIVE_SESSION)
                },
                onNavigateFriends = { navController.navigate(Destinations.FRIENDS) },
                onNavigateHistory = { navController.navigate(Destinations.HISTORY) }
            )
        }

        // 04 ActiveSession
        composable(Destinations.ACTIVE_SESSION) {
            ActiveSessionScreen(
                viewModel = activeSessionViewModel,
                onChangeExercise = { navController.navigate(Destinations.EXERCISE_PICKER) },
                onSessionFinished = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.ACTIVE_SESSION) { inclusive = true }
                    }
                }
            )
        }

        // 08 ExercisePicker
        composable(Destinations.EXERCISE_PICKER) {
            ExercisePickerScreen(
                viewModel = exercisesViewModel,
                onSelectExercise = { selectedExercise ->
                    activeSessionViewModel.selectExercise(selectedExercise)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 07 Friends
        composable(Destinations.FRIENDS) {
            FriendsScreen(
                viewModel = participantsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 05 History & 06 SessionDetail
        composable(Destinations.HISTORY) {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
