package uz.angrykitten.omerta.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors

/**
 * "Glass" effect approximation. Real Compose blur is GPU-expensive and only
 * fully supported on API 31+. We fake it cheaply by layering:
 *   1. Semi-transparent fill biased warm or cool per theme.
 *   2. Subtle inner highlight on the top edge (vertical gradient stop).
 *   3. Gradient-stroked border: warm divider top → faded bottom.
 *
 * Reads as "glass-on-noir" — translucent, depth-rich, frame-stable.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    glowAccent: Color = LocalOmertaColors.current.glow,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit,
) {
    val omerta = LocalOmertaColors.current
    val baseFill = if (omerta.isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E1E2A).copy(alpha = 0.78f),
                Color(0xFF16161E).copy(alpha = 0.72f),
                Color(0xFF101018).copy(alpha = 0.78f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF7E5).copy(alpha = 0.85f),
                Color(0xFFF1E3C2).copy(alpha = 0.80f),
                Color(0xFFE6D5AE).copy(alpha = 0.85f),
            ),
        )
    }
    val outerBorder = Brush.verticalGradient(
        colors = listOf(
            omerta.divider.copy(alpha = 0.9f),
            omerta.divider.copy(alpha = 0.35f),
        ),
    )
    val innerGleam = Brush.verticalGradient(
        colors = listOf(
            glowAccent.copy(alpha = 0.30f),
            Color.Transparent,
            Color.Transparent,
        ),
        endY = 100f,
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(baseFill)
            .background(innerGleam)
            .border(width = 1.dp, brush = outerBorder, shape = shape)
            .padding(contentPadding),
    ) {
        content()
    }
}
