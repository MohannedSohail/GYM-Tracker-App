package com.gymtracker.app.ui.exercises

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.local.entity.ExerciseEntity
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
fun ExercisePickerScreen(
    viewModel: ExercisesViewModel,
    onSelectExercise: (ExerciseEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()

    var showAddCustomDialog by remember { mutableStateOf(false) }

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
                    text = "اختيار التمرين",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerraOnSurface
                )
                TextButton(onClick = onNavigateBack) {
                    Text("إلغاء", color = TerraOnSurfaceVariant, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("بحث عن تمرين...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Muscle Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MuscleCategories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    val bg = if (isSelected) TerraPrimaryContainer else TerraSurfaceVariant
                    val textColor = if (isSelected) TerraOnPrimaryContainer else TerraOnSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { viewModel.selectCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercises List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExercises) { exercise ->
                    TerraCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectExercise(exercise) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = exercise.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerraOnSurface
                                )
                                Text(
                                    text = exercise.category,
                                    fontSize = 13.sp,
                                    color = TerraOnSurfaceVariant
                                )
                            }
                            if (exercise.isCustom) {
                                Text(
                                    text = "مخصص",
                                    fontSize = 12.sp,
                                    color = TerraPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TerraPrimaryButton(
                text = "+ إضافة تمرين مخصص",
                onClick = { showAddCustomDialog = true }
            )
        }
    }

    if (showAddCustomDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("الصدر") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("إضافة تمرين جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم التمرين") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("العضلة المستهدفة (مثلاً الصدر، الأرجل)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && category.isNotBlank()) {
                            viewModel.addCustomExercise(name, category) {
                                showAddCustomDialog = false
                            }
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
