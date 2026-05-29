package uz.angrykitten.omerta.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 8dp base grid. Use these tokens — do not hardcode dp values in screens.
 */
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 20.dp,
    val sectionGap: Dp = 24.dp,
    val minTapTarget: Dp = 48.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }
