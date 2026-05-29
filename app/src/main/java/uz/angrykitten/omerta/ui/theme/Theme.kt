package uz.angrykitten.omerta.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalOmertaColors = compositionLocalOf<OmertaColors> {
    error("OmertaColors not provided — wrap content in OmertaTheme")
}

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF1A1A1A),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFE74C3C),
    onError = DarkOnPrimary,
)

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = Color(0xFFFDF3E3),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = Color(0xFFC0392B),
    onError = LightOnPrimary,
)

private val DarkOmertaColors = OmertaColors(
    teamMafia = DarkTeamMafia,
    teamTown = DarkTeamTown,
    teamNeutral = DarkTeamNeutral,
    success = DarkSuccess,
    cardSurface = DarkSurfaceVariant,
    cardSurfaceTop = Color(0xFF20202E),
    cardSurfaceBottom = Color(0xFF161620),
    glow = DarkSecondary.copy(alpha = 0.35f),
    divider = DarkOutline,
    isDark = true,
)

private val LightOmertaColors = OmertaColors(
    teamMafia = LightTeamMafia,
    teamTown = LightTeamTown,
    teamNeutral = LightTeamNeutral,
    success = LightSuccess,
    cardSurface = LightSurfaceVariant,
    cardSurfaceTop = Color(0xFFFFF6E0),
    cardSurfaceBottom = Color(0xFFEDDFBE),
    glow = LightSecondary.copy(alpha = 0.45f),
    divider = LightOutline,
    isDark = false,
)

@Composable
fun OmertaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val omertaColors = if (darkTheme) DarkOmertaColors else LightOmertaColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // AUDIT FIX: only adjust status-bar text color (true icons-on-dark
            // background = light icons). Setting statusBarColor itself is a no-op
            // under edge-to-edge mode.
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // AUDIT FIX: provide Spacing alongside OmertaColors so screens don't have to
    // worry about ordering when both are needed.
    CompositionLocalProvider(
        LocalOmertaColors provides omertaColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OmertaTypography,
            content = content,
        )
    }
}
