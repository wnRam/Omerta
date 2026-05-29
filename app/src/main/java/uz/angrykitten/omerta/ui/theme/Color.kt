package uz.angrykitten.omerta.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Project-wide color tokens. Two palettes (dark = noir, light = parchment).
 * Material3's ColorScheme covers most surfaces, but we also expose team colors
 * via [OmertaColors] (a CompositionLocal) so any composable can read them
 * without prop-drilling.
 */

// ---- Dark (noir) ----
val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF12121A)
val DarkSurfaceVariant = Color(0xFF1C1C28)
val DarkPrimary = Color(0xFFC0392B)
val DarkSecondary = Color(0xFFF1C40F)
val DarkOnPrimary = Color(0xFFF5F5F5)
val DarkOnSurface = Color(0xFFF5F5F5)
val DarkOnSurfaceVariant = Color(0xFF9E9E9E)
val DarkOutline = Color(0xFF2A2A38)
val DarkSuccess = Color(0xFF27AE60)
val DarkTeamMafia = Color(0xFF922B21)
val DarkTeamTown = Color(0xFF1A5276)
val DarkTeamNeutral = Color(0xFF6C3483)

// ---- Light (parchment) ----
val LightBackground = Color(0xFFF5ECD7)
val LightSurface = Color(0xFFEDE0C4)
val LightSurfaceVariant = Color(0xFFFDF3E3)
val LightPrimary = Color(0xFF922B21)
val LightSecondary = Color(0xFFB7950B)
val LightOnPrimary = Color(0xFFFDF3E3)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnSurfaceVariant = Color(0xFF5D5D5D)
val LightOutline = Color(0xFFC9B68A)
val LightSuccess = Color(0xFF1E8449)
val LightTeamMafia = Color(0xFF7B1F18)
val LightTeamTown = Color(0xFF154360)
val LightTeamNeutral = Color(0xFF512E5F)

/** Team / status colors not represented in Material3 ColorScheme. */
@Immutable
data class OmertaColors(
    val teamMafia: Color,
    val teamTown: Color,
    val teamNeutral: Color,
    val success: Color,
    val cardSurface: Color,
    val cardSurfaceTop: Color,
    val cardSurfaceBottom: Color,
    val glow: Color,
    val divider: Color,
    val isDark: Boolean,
)
