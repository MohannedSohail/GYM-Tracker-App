package com.gymtracker.app.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import com.gymtracker.app.ui.components.FriendAvatar
import com.gymtracker.app.ui.components.TerraCard
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraOnSurface
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant
import com.gymtracker.app.ui.theme.TerraOutline
import com.gymtracker.app.ui.theme.TerraPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val completedSessions by viewModel.completedSessions.collectAsState()
    val selectedDetail by viewModel.selectedSessionDetail.collectAsState()

    val dateFormat = SimpleDateFormat("dd MMMM yyyy - hh:mm a", Locale("ar"))

    Scaffold(
        containerColor = TerraBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDetail != null) "تفاصيل الجلسة" else "سجل التمارين",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerraOnSurface
                )
                TextButton(
                    onClick = {
                        if (selectedDetail != null) {
                            viewModel.clearSelectedSession()
                        } else {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("رجوع", color = TerraOnSurfaceVariant, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedDetail != null) {
                // 06 SessionDetail View
                val detail = selectedDetail!!
                val session = detail.session

                Text(
                    text = session?.let { dateFormat.format(Date(it.startTime)) } ?: "",
                    fontSize = 14.sp,
                    color = TerraOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group sets by exercise
                    val setsByExercise = detail.sets.groupBy { it.exerciseId }

                    items(setsByExercise.toList()) { (exerciseId, exerciseSets) ->
                        val exercise = detail.exercisesMap[exerciseId]
                        TerraCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = exercise?.name ?: "تمرين",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerraPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                exerciseSets.forEach { set ->
                                    val participant = detail.participants.find { it.id == set.participantId }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${participant?.name ?: ""}: جولة ${set.setNumber}",
                                            fontSize = 15.sp,
                                            color = TerraOnSurface
                                        )
                                        Text(
                                            text = "${set.weightKg} كجم × ${set.reps} عدات",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerraOnSurface
                                        )
                                    }
                                    HorizontalDivider(color = TerraOutline.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            } else {
                // 05 History List View
                if (completedSessions.isEmpty()) {
                    TerraCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "لا يوجد سجل تمارين سابق حتى الآن",
                                fontSize = 16.sp,
                                color = TerraOnSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(completedSessions) { session ->
                            TerraCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadSessionDetails(session) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = dateFormat.format(Date(session.startTime)),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TerraOnSurface
                                        )
                                        val durationMinutes = session.endTime?.let {
                                            ((it - session.startTime) / 60000).coerceAtLeast(1)
                                        } ?: 0
                                        Text(
                                            text = "المدة: $durationMinutes دقيقة",
                                            fontSize = 13.sp,
                                            color = TerraOnSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "عرض التفاصيل ←",
                                        color = TerraPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
