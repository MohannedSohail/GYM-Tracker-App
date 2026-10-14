package com.gymtracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = TerraPrimary,
    onPrimary = TerraOnPrimary,
    primaryContainer = TerraPrimaryContainer,
    onPrimaryContainer = TerraOnPrimaryContainer,
    secondary = TerraSecondary,
    onSecondary = TerraOnSecondary,
    secondaryContainer = TerraSecondaryContainer,
    onSecondaryContainer = TerraOnSecondaryContainer,
    tertiary = TerraTertiary,
    onTertiary = TerraOnTertiary,
    tertiaryContainer = TerraTertiaryContainer,
    background = TerraBackground,
    surface = TerraSurface,
    surfaceVariant = TerraSurfaceVariant,
    outline = TerraOutline,
    onSurface = TerraOnSurface,
    onSurfaceVariant = TerraOnSurfaceVariant,
    error = TerraError,
    errorContainer = TerraErrorContainer
)

@Composable
fun TerraGymTheme(
    content: @Composable () -> Unit
) {
    // Spec & Stitch platform rule: Arabic only, full RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            content = content
        )
    }
}
