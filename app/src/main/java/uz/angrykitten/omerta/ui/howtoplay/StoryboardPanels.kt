package uz.angrykitten.omerta.ui.howtoplay

import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.icons.RoleIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.cardGradient

/**
 * Nine cinematic panels for the rules tab. Each panel exposes [Render] which
 * draws a stylized scene + narration. Animations are intentionally light —
 * sustained motion across 9 always-visible panels would tax the GPU; instead
 * we use slow pulses + occasional ambient particles.
 */
sealed class StoryboardPanel {
    @Composable
    abstract fun Render()

    object Setup : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_setup_title,
            bodyRes = R.string.story_setup_body,
        ) {
            CircleOfSilhouettes()
        }
    }

    object Night : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_night_title,
            bodyRes = R.string.story_night_body,
            bgColors = listOf(Color(0xFF050514), Color(0xFF0A0A1E)),
        ) {
            MoonPulseScene()
        }
    }

    object MafiaChooses : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_mafia_title,
            bodyRes = R.string.story_mafia_body,
            bgColors = listOf(Color(0xFF1A0606), Color(0xFF050514)),
        ) {
            IconCenterpiece(icon = RoleIcons.Mafia, tint = LocalOmertaColors.current.teamMafia)
        }
    }

    object Doctor : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_doctor_title,
            bodyRes = R.string.story_doctor_body,
        ) {
            IconCenterpiece(icon = RoleIcons.Doctor, tint = LocalOmertaColors.current.success)
        }
    }

    object Sheriff : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_sheriff_title,
            bodyRes = R.string.story_sheriff_body,
        ) {
            IconCenterpiece(icon = RoleIcons.Sheriff, tint = LocalOmertaColors.current.teamTown)
        }
    }

    object Day : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_day_title,
            bodyRes = R.string.story_day_body,
            bgColors = listOf(Color(0xFFFFD78F), Color(0xFFF1A661)),
            titleColor = Color(0xFF2A1A0A),
            bodyColor = Color(0xFF2A1A0A),
        ) {
            SunRaysScene()
        }
    }

    object Vote : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_vote_title,
            bodyRes = R.string.story_vote_body,
            bgColors = listOf(Color(0xFF6B1A14), Color(0xFF3D0E0A)),
        ) {
            IconCenterpiece(icon = OmertaIcons.Vote, tint = Color(0xFFF1C40F))
        }
    }

    object WinConditions : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_win_title,
            bodyRes = R.string.story_win_body,
        ) {
            WinSplitScene()
        }
    }

    object QuickReference : StoryboardPanel() {
        @Composable
        override fun Render() = PanelLayout(
            titleRes = R.string.story_ref_title,
            bodyRes = R.string.story_ref_body,
        ) {
            QuickRoleGrid()
        }
    }
}

object StoryboardPanels {
    val list: List<StoryboardPanel> = listOf(
        StoryboardPanel.Setup,
        StoryboardPanel.Night,
        StoryboardPanel.MafiaChooses,
        StoryboardPanel.Doctor,
        StoryboardPanel.Sheriff,
        StoryboardPanel.Day,
        StoryboardPanel.Vote,
        StoryboardPanel.WinConditions,
        StoryboardPanel.QuickReference,
    )
}

@Composable
private fun PanelLayout(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    bgColors: List<Color> = emptyList(),
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    bodyColor: Color = MaterialTheme.colorScheme.onSurface,
    illustration: @Composable () -> Unit,
) {
    val omerta = LocalOmertaColors.current
    val brush = if (bgColors.isEmpty()) cardGradient()
    else Brush.verticalGradient(colors = bgColors)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .border(1.dp, omerta.divider, RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) { illustration() }
        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = titleColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = bodyColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CircleOfSilhouettes() {
    val omerta = LocalOmertaColors.current
    Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(size.width, size.height) * 0.32f
        val count = 8
        for (i in 0 until count) {
            val angle = (i * (2 * Math.PI / count) - Math.PI / 2).toFloat()
            val x = cx + r * kotlin.math.cos(angle.toDouble()).toFloat()
            val y = cy + r * kotlin.math.sin(angle.toDouble()).toFloat()
            drawCircle(
                color = omerta.cardSurfaceTop,
                radius = 22f,
                center = Offset(x, y),
            )
            drawCircle(
                color = Color(0xFFF1C40F).copy(alpha = 0.6f),
                radius = 22f,
                center = Offset(x, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
            )
        }
    }
}

@Composable
private fun MoonPulseScene() {
    val infinite = rememberInfiniteTransition(label = "moon")
    val pulse by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "moon.pulse",
    )
    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size((160 * pulse).dp)
                .clip(CircleShape)
                .background(Color(0xFFF1C40F).copy(alpha = 0.18f)),
        )
        Icon(
            imageVector = OmertaIcons.Night,
            contentDescription = null,
            tint = Color(0xFFFFF5C0),
            modifier = Modifier.size(96.dp),
        )
    }
}

@Composable
private fun SunRaysScene() {
    val infinite = rememberInfiniteTransition(label = "sun")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(28_000, easing = LinearEasing), RepeatMode.Restart),
        label = "sun.rot",
    )
    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            for (i in 0 until 12) {
                val a = (rotation + i * 30) * Math.PI / 180
                val x1 = cx + 60f * kotlin.math.cos(a).toFloat()
                val y1 = cy + 60f * kotlin.math.sin(a).toFloat()
                val x2 = cx + 110f * kotlin.math.cos(a).toFloat()
                val y2 = cy + 110f * kotlin.math.sin(a).toFloat()
                drawLine(
                    color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
        Icon(
            imageVector = OmertaIcons.Day,
            contentDescription = null,
            tint = Color(0xFFFFFFFF),
            modifier = Modifier.size(110.dp),
        )
    }
}

@Composable
private fun IconCenterpiece(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    val infinite = rememberInfiniteTransition(label = "icon.pulse")
    val glow by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "icon.glow",
    )
    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.18f * glow)),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(110.dp),
        )
    }
}

@Composable
private fun WinSplitScene() {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WinSide(
            color = LocalOmertaColors.current.teamTown,
            labelRes = R.string.team_town,
            modifier = Modifier.weight(1f),
        )
        WinSide(
            color = LocalOmertaColors.current.teamMafia,
            labelRes = R.string.team_mafia,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WinSide(color: Color, @StringRes labelRes: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuickRoleGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val keys = uz.angrykitten.omerta.ui.icons.RoleIconRegistry.allKeys.take(8)
        keys.chunked(4).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LocalOmertaColors.current.cardSurfaceTop),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = uz.angrykitten.omerta.ui.icons.RoleIconRegistry.forKey(key),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
        }
    }
}
