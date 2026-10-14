package com.gymtracker.app.ui.participants

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.local.entity.ParticipantEntity
import com.gymtracker.app.ui.components.FriendAvatar
import com.gymtracker.app.ui.components.TerraCard
import com.gymtracker.app.ui.components.TerraPrimaryButton
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraError
import com.gymtracker.app.ui.theme.TerraOnSurface
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant

@Composable
fun FriendsScreen(
    viewModel: ParticipantsViewModel,
    onNavigateBack: () -> Unit
) {
    val activeParticipants by viewModel.activeParticipants.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "الأصدقاء",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerraOnSurface
                )
                TextButton(onClick = onNavigateBack) {
                    Text("رجوع", color = TerraOnSurfaceVariant, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "قائمة الأصدقاء المشاركين في التمرين (2 إلى 3 أشخاص):",
                fontSize = 14.sp,
                color = TerraOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Friends List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(activeParticipants) { index, friend ->
                    FriendItemCard(
                        friend = friend,
                        index = index,
                        onEdit = { viewModel.selectForEdit(friend) },
                        onDelete = { viewModel.selectForDelete(friend) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TerraPrimaryButton(
                text = "+ إضافة صديق جديد",
                onClick = { showAddDialog = true }
            )
        }
    }

    // Add Friend Dialog (d5 AddEditFriendDialog)
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة صديق جديد", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addParticipant(newName) { showAddDialog = false }
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Edit Friend Dialog
    uiState.selectedParticipantForEdit?.let { friend ->
        var editName by remember { mutableStateOf(friend.name) }
        AlertDialog(
            onDismissRequest = { viewModel.selectForEdit(null) },
            title = { Text("تعديل اسم الصديق", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.renameParticipant(friend.id, editName)
                        }
                    }
                ) {
                    Text("تحديث")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectForEdit(null) }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Friend Dialog (d6 DeleteFriendDialog - Soft Delete explanation)
    uiState.selectedParticipantForDelete?.let { friend ->
        AlertDialog(
            onDismissRequest = { viewModel.selectForDelete(null) },
            title = { Text("حذف ${friend.name}؟", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "سيتم إخفاء ${friend.name} من قائمة بدء التمارين الجديدة، مع الاحتفاظ التام بكافة جولاته وأوزانه المسجلة سابقاً في سجل الـ History."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteParticipant(friend.id) }
                ) {
                    Text("تأكيد الحذف", color = TerraError)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectForDelete(null) }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun FriendItemCard(
    friend: ParticipantEntity,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TerraCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FriendAvatar(name = friend.name, index = index)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = friend.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TerraOnSurface
                )
            }

            Row {
                TextButton(onClick = onEdit) {
                    Text("تعديل", color = TerraOnSurfaceVariant)
                }
                TextButton(onClick = onDelete) {
                    Text("حذف", color = TerraError)
                }
            }
        }
    }
}
