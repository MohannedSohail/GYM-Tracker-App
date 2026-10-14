package com.gymtracker.app.ui.participants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.ui.components.TerraPrimaryButton
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraOnSurface
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant

@Composable
fun FirstRunSetupScreen(
    onSetupComplete: (List<String>) -> Unit
) {
    var friend1 by remember { mutableStateOf("") }
    var friend2 by remember { mutableStateOf("") }
    var friend3 by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TerraBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "أهلاً بك في جيم",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerraOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "أدخل أسماء أصدقائك الذين ستتمرن معهم (2 إلى 3 أشخاص). سيتم حفظ الأسماء لجلساتك القادمة:",
                    fontSize = 15.sp,
                    color = TerraOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = friend1,
                    onValueChange = { friend1 = it },
                    label = { Text("الصديق الأول") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = friend2,
                    onValueChange = { friend2 = it },
                    label = { Text("الصديق الثاني (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = friend3,
                    onValueChange = { friend3 = it },
                    label = { Text("الصديق الثالث (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val validNames = listOf(friend1, friend2, friend3).map { it.trim() }.filter { it.isNotBlank() }

            TerraPrimaryButton(
                text = "ابدأ التمرين",
                onClick = { onSetupComplete(validNames) },
                enabled = validNames.isNotEmpty()
            )
        }
    }
}
