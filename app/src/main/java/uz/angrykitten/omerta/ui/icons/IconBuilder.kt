package uz.angrykitten.omerta.ui.icons

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Compose-friendly factory for our custom icons. All Omerta icons live on a
 * 24×24 view box and use `Color.Unspecified` for paint so callers can tint
 * via Material3's `Icon(... tint = …)`.
 *
 * Stroke caps / joins default to round (per spec: "rounded caps"). Two
 * weights:
 *   - [solid] — filled silhouettes (Mafia, Crown, Play)
 *   - [outline] — line art (Settings, Eye, Door) — default 2.2dp stroke
 *   - [accent] — slim 1.4dp stroke for layered details on top of solids
 */
internal fun omertaIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = "Omerta.$name",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = autoMirror,
).apply(block).build()

internal fun ImageVector.Builder.solid(block: PathBuilder.() -> Unit) {
    path(
        fill = SolidColor(androidx.compose.ui.graphics.Color.Black),
        pathFillType = PathFillType.NonZero,
    ) { block() }
}

internal fun ImageVector.Builder.outline(
    strokeWidth: Float = 2.2f,
    block: PathBuilder.() -> Unit,
) {
    path(
        stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) { block() }
}

/** Slimmer accent stroke layered over a [solid] base. */
internal fun ImageVector.Builder.accent(
    strokeWidth: Float = 1.4f,
    block: PathBuilder.() -> Unit,
) {
    path(
        stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) { block() }
}

internal fun ImageVector.Builder.solidEvenOdd(block: PathBuilder.() -> Unit) {
    path(
        fill = SolidColor(androidx.compose.ui.graphics.Color.Black),
        pathFillType = PathFillType.EvenOdd,
    ) { block() }
}
