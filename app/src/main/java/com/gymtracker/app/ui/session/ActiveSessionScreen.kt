package com.gymtracker.app.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.ui.components.FriendAvatar
import com.gymtracker.app.ui.components.StepperButton
import com.gymtracker.app.ui.components.TerraCard
import com.gymtracker.app.ui.components.TerraPrimaryButton
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraError
import com.gymtracker.app.ui.theme.TerraOnPrimaryContainer
import com.gymtracker.app.ui.theme.TerraOnSurface
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant
import com.gymtracker.app.ui.theme.TerraOutline
import com.gymtracker.app.ui.theme.TerraPrimary
import com.gymtracker.app.ui.theme.TerraPrimaryContainer
import com.gymtracker.app.ui.theme.TerraSurfaceVariant

@Composable
fun ActiveSessionScreen(
    viewModel: ActiveSessionViewModel,
    onChangeExercise: () -> Unit,
    onSessionFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showWeightEditDialog by remember { mutableStateOf(false) }
    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onSessionFinished()
        }
    }

    Scaffold(
        containerColor = TerraBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.currentExercise?.name ?: "اختر تمريناً",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerraOnSurface
                    )
                    TextButton(onClick = onChangeExercise) {
                        Text("تغيير التمرين", color = TerraPrimary, fontSize = 13.sp)
                    }
                }

                TextButton(onClick = { showFinishConfirmDialog = true }) {
                    Text("إنهاء التمرين", color = TerraError, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Participant Turn Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(uiState.participants) { index, participant ->
                    val isSelected = uiState.selectedParticipantId == participant.id
                    val bg = if (isSelected) TerraPrimaryContainer else TerraSurfaceVariant
                    val textColor = if (isSelected) TerraOnPrimaryContainer else TerraOnSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .clickable { viewModel.selectParticipant(participant.id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FriendAvatar(name = participant.name, index = index, size = 28.dp, fontSize = 12)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = participant.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Previous Reference Target
            uiState.previousPerformance?.let { prev ->
                TerraCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "آخر أداء مسجل:",
                            fontSize = 13.sp,
                            color = TerraOnSurfaceVariant
                        )
                        Text(
                            text = "${prev.weightKg} كجم × ${prev.reps} عدات",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerraPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Steppers Card
            TerraCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Weight Stepper
                    Text(
                        text = "الوزن (كجم)",
                        fontSize = 14.sp,
                        color = TerraOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepperButton(
                            label = "-2.5",
                            onClick = { viewModel.onWeightDecrement(2.5) }
                        )

                        // Hero Number (Clickable to type decimal exact)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showWeightEditDialog = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.weightInputKg % 1.0 == 0.0) {
                                    "${uiState.weightInputKg.toInt()}"
                                } else {
                                    "${uiState.weightInputKg}"
                                },
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerraOnSurface
                            )
                        }

                        StepperButton(
                            label = "+2.5",
                            onClick = { viewModel.onWeightIncrement(2.5) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = TerraOutline)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reps Stepper
                    Text(
                        text = "العدّات",
                        fontSize = 14.sp,
                        color = TerraOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepperButton(
                            label = "-1",
                            onClick = { viewModel.onRepsDecrement() }
                        )

                        Text(
                            text = "${uiState.repsInput}",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerraOnSurface
                        )

                        StepperButton(
                            label = "+1",
                            onClick = { viewModel.onRepsIncrement() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: "تسجيل الجولة"
                    TerraPrimaryButton(
                        text = "تسجيل الجولة",
                        onClick = { viewModel.logRound() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logged Sets in this Exercise
            Text(
                text = "الجولات المسجلة في هذا التمرين:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TerraOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.loggedSets) { set ->
                    val participant = uiState.participants.find { it.id == set.participantId }
                    TerraCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جولة ${set.setNumber} - ${participant?.name ?: ""}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TerraOnSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${set.weightKg} كجم × ${set.reps} عدات",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerraPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { viewModel.deleteRound(set.id, set.participantId) }
                                ) {
                                    Text("حذف", color = TerraError, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Weight Type Dialog (AC-7)
    if (showWeightEditDialog) {
        var typedWeight by remember { mutableStateOf("${uiState.weightInputKg}") }
        AlertDialog(
            onDismissRequest = { showWeightEditDialog = false },
            title = { Text("كتابة الوزن يدوياً (كجم)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = typedWeight,
                    onValueChange = { typedWeight = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    label = { Text("مثال: 22.5") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = typedWeight.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            viewModel.onWeightDirectInput(parsed)
                        }
                        showWeightEditDialog = false
                    }
                ) {
                    Text("تطبيق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Finish Workout Confirmation Dialog
    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = { Text("إنهاء التمرين؟", fontWeight = FontWeight.Bold) },
            text = { Text("هل تود حفظ وإنهاء جلسة التمرين الحالية؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.finishSession()
                        showFinishConfirmDialog = false
                    }
                ) {
                    Text("نعم، إنهاء", color = TerraPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("متابعة التمرين")
                }
            }
        )
    }
}
