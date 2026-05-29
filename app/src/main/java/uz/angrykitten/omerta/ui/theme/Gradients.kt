package uz.angrykitten.omerta.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp

/**
 * Radial gradient for the app background — gives the "depth" requirement
 * without forcing every screen to draw its own.
 */
@Composable
@ReadOnlyComposable
fun backgroundGradient(): Brush {
    val scheme = MaterialThemeAlias.colorScheme()
    val warmCenter = lerp(scheme.background, scheme.primary, 0.05f)
    return Brush.radialGradient(
        colors = listOf(warmCenter, scheme.background),
        radius = 1600f,
    )
}

@Composable
@ReadOnlyComposable
fun cardGradient(): Brush {
    val omerta = LocalOmertaColors.current
    return Brush.verticalGradient(
        colors = listOf(omerta.cardSurfaceTop, omerta.cardSurfaceBottom),
    )
}

@Composable
@ReadOnlyComposable
fun teamGradient(top: Color): Brush {
    val omerta = LocalOmertaColors.current
    val bottom = lerp(top, omerta.cardSurfaceBottom, 0.65f)
    return Brush.verticalGradient(colors = listOf(top.copy(alpha = 0.85f), bottom))
}

/** Soft glow used by active/focused elements. Caller applies with `Modifier.drawBehind`. */
@Composable
@ReadOnlyComposable
fun softGlow(color: Color = LocalOmertaColors.current.glow): Shadow =
    Shadow(color = color, blurRadius = 24f)

/**
 * Tiny shim so `backgroundGradient()` doesn't have to import MaterialTheme directly
 * (keeps the import surface in screens narrow — they only ever pull `OmertaTheme`).
 */
internal object MaterialThemeAlias {
    @Composable
    @ReadOnlyComposable
    fun colorScheme() = androidx.compose.material3.MaterialTheme.colorScheme
}
