package com.gymtracker.app.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.data.local.entity.WorkoutSessionEntity
import com.gymtracker.app.ui.components.FriendAvatar
import com.gymtracker.app.ui.components.TerraCard
import com.gymtracker.app.ui.components.TerraPrimaryButton
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraOnPrimaryContainer
import com.gymtracker.app.ui.theme.TerraOnSurface
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant
import com.gymtracker.app.ui.theme.TerraPrimary
import com.gymtracker.app.ui.theme.TerraPrimaryContainer
import com.gymtracker.app.ui.theme.TerraSurfaceVariant

@Composable
fun HomeScreen(
    activeParticipants: List<ParticipantEntity>,
    activeSession: WorkoutSessionEntity?,
    onStartSession: (selectedIds: List<Long>) -> Unit,
    onResumeSession: () -> Unit,
    onNavigateFriends: () -> Unit,
    onNavigateHistory: () -> Unit
) {
    val selectedParticipantIds = remember(activeParticipants) {
        mutableStateListOf<Long>().apply {
            addAll(activeParticipants.map { it.id })
        }
    }

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
                Column {
                    Text(
                        text = "جيم",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerraPrimary
                    )
                    Text(
                        text = "تسجيل التمارين المشتركة",
                        fontSize = 14.sp,
                        color = TerraOnSurfaceVariant
                    )
                }

                Row {
                    TextButton(onClick = onNavigateFriends) {
                        Text("الأصدقاء", color = TerraPrimary, fontSize = 16.sp)
                    }
                    TextButton(onClick = onNavigateHistory) {
                        Text("السجل", color = TerraOnSurfaceVariant, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Active Session Alert Card (if ongoing session exists)
            if (activeSession != null) {
                TerraCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تمرين جارٍ حالياً",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerraPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لديك تمرين لم ينتهِ بعد. يمكنك المتابعة وتسجيل باقي الجولات.",
                            fontSize = 14.sp,
                            color = TerraOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TerraPrimaryButton(
                            text = "متابعة التمرين",
                            onClick = onResumeSession
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Participant Selection Section
            Text(
                text = "من يتمرن اليوم؟",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TerraOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (activeParticipants.isEmpty()) {
                TerraCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لم تتم إضافة أي أصدقاء بعد",
                            color = TerraOnSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TerraPrimaryButton(
                            text = "إضافة أصدقاء",
                            onClick = onNavigateFriends
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(activeParticipants) { index, friend ->
                        val isSelected = selectedParticipantIds.contains(friend.id)
                        ParticipantSelectChip(
                            friend = friend,
                            index = index,
                            isSelected = isSelected,
                            onToggle = {
                                if (isSelected) {
                                    if (selectedParticipantIds.size > 1) {
                                        selectedParticipantIds.remove(friend.id)
                                    }
                                } else {
                                    selectedParticipantIds.add(friend.id)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Start Session Button
                TerraPrimaryButton(
                    text = "بدء تمرين جديد (${selectedParticipantIds.size} مشارك)",
                    onClick = {
                        if (selectedParticipantIds.isNotEmpty()) {
                            onStartSession(selectedParticipantIds.toList())
                        }
                    },
                    enabled = selectedParticipantIds.isNotEmpty() && activeSession == null
                )
            }
        }
    }
}

@Composable
fun ParticipantSelectChip(
    friend: ParticipantEntity,
    index: Int,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bg = if (isSelected) TerraPrimaryContainer else TerraSurfaceVariant
    val textColor = if (isSelected) TerraOnPrimaryContainer else TerraOnSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FriendAvatar(name = friend.name, index = index, size = 32.dp, fontSize = 14)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = friend.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}
