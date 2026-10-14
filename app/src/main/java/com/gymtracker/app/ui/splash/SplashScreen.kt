package com.gymtracker.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.ui.theme.TerraBackground
import com.gymtracker.app.ui.theme.TerraOnSurfaceVariant
import com.gymtracker.app.ui.theme.TerraPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerraBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "جيم",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = TerraPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "تسجيل التمارين المشتركة",
                fontSize = 16.sp,
                color = TerraOnSurfaceVariant
            )
        }
    }
}
