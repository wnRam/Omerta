package uz.angrykitten.omerta.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.MonoFamily

/**
 * Atmospheric multi-layer background.
 *
 *   Layer 1: vertical gradient — moody top fading deeper toward bottom.
 *   Layer 2: large soft radial highlight off the top-right (side lighting).
 *   Layer 3 (dark only): thin diagonal hatch lines for texture.
 *
 * Drop in for hero screens (Home, End Game). Cheaper than backdrop blur.
 */
@Composable
fun DepthBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val omerta = LocalOmertaColors.current

    val base = if (omerta.isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A0A14),
                Color(0xFF0E0810),
                Color(0xFF050308),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF5ECD7),
                Color(0xFFEDE0C4),
                Color(0xFFD8C9A4),
            ),
        )
    }

    val highlight = if (omerta.isDark) {
        Brush.radialGradient(
            colors = listOf(Color(0xFFC0392B).copy(alpha = 0.22f), Color.Transparent),
            center = Offset(900f, 200f),
            radius = 1100f,
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFFF1C40F).copy(alpha = 0.20f), Color.Transparent),
            center = Offset(900f, 200f),
            radius = 1100f,
        )
    }

    Box(modifier = modifier.fillMaxSize().background(base).background(highlight)) {
        if (omerta.isDark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacing = 40f
                val color = Color(0xFFF1C40F).copy(alpha = 0.04f)
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = color,
                        start = Offset(x, 0f),
                        end = Offset(x + size.height, size.height),
                        strokeWidth = 1f,
                    )
                    x += spacing
                }
            }
        }
        content()
    }
}

/**
 * Cinematic numbered marker — monospace ordinal + spaced label + trailing rule.
 * Drops in as a section header.
 */
@Composable
fun ChapterMarker(
    ordinal: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val omerta = LocalOmertaColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ordinal,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = MonoFamily,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .fillMaxWidth(),
        ) {
            drawLine(
                color = omerta.divider,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 1f,
            )
        }
    }
}
