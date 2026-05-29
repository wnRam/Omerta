package uz.angrykitten.omerta.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import uz.angrykitten.omerta.OmertaApp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.game.GameSession
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team
import uz.angrykitten.omerta.domain.model.localizedDescription
import uz.angrykitten.omerta.domain.model.localizedName
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.icons.RoleIconRegistry
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.theme.cardGradient

/**
 * The cinematic centerpiece. State machine:
 *
 *   IDLE (face-down, gently bobbing) →
 *   SUSPENSE (1.5s screen-darken + glow) →
 *   FLIP (0.5s Y-axis rotation; halfway through, face swaps) →
 *   REVEALED (face-up with particle burst, settles via spring()) →
 *   PASS (Local: "Pass to next" button → back to IDLE for next player.
 *         LAN: stays revealed; "Hide my role" toggle).
 */
@Composable
fun RoleRevealScreen(
    onAllRevealed: () -> Unit,
) {
    val gameSession = remember { OmertaApp.get().gameSession }
    val session by gameSession.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val spacing = LocalSpacing.current

    val players = session.players
    if (players.isEmpty()) {
        // Shouldn't normally happen; render an empty state so we never crash.
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { }
        return
    }

    val cursor = session.revealCursor.coerceAtMost(players.size - 1)
    val current = players[cursor]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RevealHeader(
                currentIndex = cursor + 1,
                total = players.size,
                playerName = current.name,
            )
            Spacer(modifier = Modifier.weight(1f))
            RoleRevealCard(
                player = current,
                language = language,
                onRevealComplete = { /* handled internally */ },
            )
            Spacer(modifier = Modifier.weight(1f))
            RevealFooter(
                session = gameSession,
                mode = session.mode,
                isLast = cursor == players.size - 1,
                onAllRevealed = onAllRevealed,
            )
        }
    }
}

@Composable
private fun RevealHeader(currentIndex: Int, total: Int, playerName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.reveal_progress, currentIndex, total),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = R.string.reveal_pass_to, playerName),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RevealFooter(
    session: GameSession,
    mode: GameMode,
    isLast: Boolean,
    onAllRevealed: () -> Unit,
) {
    PrimaryButton(
        text = if (isLast) stringResource(id = R.string.reveal_done_all)
        else stringResource(id = R.string.reveal_pass_next),
        onClick = {
            if (isLast) {
                session.beginNight()
                onAllRevealed()
            } else {
                session.advanceReveal()
            }
        },
    )
}

private enum class RevealStage { IDLE, SUSPENSE, FLIP, REVEALED }

@Composable
private fun RoleRevealCard(
    player: Player,
    language: uz.angrykitten.omerta.domain.model.AppLanguage,
    onRevealComplete: () -> Unit,
) {
    var stage by remember(player.id) { mutableStateOf(RevealStage.IDLE) }
    val role = player.assignedRole

    LaunchedEffect(stage, player.id) {
        when (stage) {
            RevealStage.SUSPENSE -> {
                delay(1500); stage = RevealStage.FLIP
            }
            RevealStage.FLIP -> {
                delay(500); stage = RevealStage.REVEALED; onRevealComplete()
            }
            else -> Unit
        }
    }

    // Face-down bob
    val infinite = rememberInfiniteTransition(label = "reveal.bob")
    val bob by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "reveal.bob.value",
    )

    // Flip rotation: 0 -> -90 -> 0 (front face shown after 90)
    val rotation by animateFloatAsState(
        targetValue = when (stage) {
            RevealStage.IDLE, RevealStage.SUSPENSE -> 0f
            RevealStage.FLIP -> -90f
            RevealStage.REVEALED -> 0f
        },
        animationSpec = when (stage) {
            RevealStage.REVEALED -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            else -> tween(durationMillis = 320, easing = LinearEasing)
        },
        label = "reveal.rotation",
    )

    val showFront = stage == RevealStage.REVEALED

    Box(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(0.72f)
            .clickable(
                role = SemanticRole.Button,
                enabled = stage == RevealStage.IDLE,
                onClick = { stage = RevealStage.SUSPENSE },
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
                translationY = if (stage == RevealStage.IDLE) bob else 0f
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showFront && role != null) {
            CardFrontFace(role = role, language = language)
        } else {
            CardBackFace()
        }
        if (showFront && role != null) {
            ParticleBurst(team = role.team)
        }
    }
}

@Composable
private fun CardBackFace() {
    val omerta = LocalOmertaColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(cardGradient())
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                shape = RoundedCornerShape(24.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Ornate geometric back — a star + concentric circles in gold.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseR = minOf(size.width, size.height) * 0.38f
            // Concentric circles
            for (k in 0..3) {
                drawCircle(
                    color = Color(0xFFF1C40F).copy(alpha = 0.18f - k * 0.04f),
                    radius = baseR + k * 14f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                )
            }
            // 8-point star
            val points = 16
            val rOuter = baseR * 0.95f
            val rInner = baseR * 0.45f
            val path = androidx.compose.ui.graphics.Path()
            for (i in 0 until points) {
                val angle = (i * Math.PI / (points / 2)) - Math.PI / 2
                val r = if (i % 2 == 0) rOuter else rInner
                val x = (cx + r * cos(angle)).toFloat()
                val y = (cy + r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path = path, color = Color(0xFFF1C40F).copy(alpha = 0.32f))
            drawPath(
                path = path,
                color = Color(0xFFF1C40F).copy(alpha = 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
            )
        }
        Text(
            text = "Ω",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
            ),
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun CardFrontFace(role: Role, language: uz.angrykitten.omerta.domain.model.AppLanguage) {
    val omerta = LocalOmertaColors.current
    val accent = when (role.team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
    }
    val gradient = Brush.verticalGradient(
        colors = listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.4f), omerta.cardSurfaceBottom),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(width = 2.dp, color = accent, shape = RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = RoleIconRegistry.forKey(role.iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(120.dp),
        )
        Text(
            text = role.localizedName(language),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        TeamBadge(team = role.team)
        Text(
            text = role.localizedDescription(language),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TeamBadge(team: Team) {
    val labelRes = when (team) {
        Team.MAFIA -> R.string.team_mafia
        Team.TOWN -> R.string.team_town
        Team.NEUTRAL -> R.string.team_neutral
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Particle burst at reveal. 60 particles fan out from card center, colored
 * by the role's team, with random angle + random radius + random easing
 * resulting in an explosion that decays over ~1.2 seconds.
 */
@Composable
private fun ParticleBurst(team: Team) {
    val omerta = LocalOmertaColors.current
    val color = when (team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
    }
    val particles = remember { generateBurstParticles(60) }
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        while (progress < 1f) {
            val t = ((System.currentTimeMillis() - start) / 1200f).coerceAtMost(1f)
            progress = t
            kotlinx.coroutines.delay(16)
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = minOf(size.width, size.height) * 0.55f
        particles.forEach { p ->
            val eased = 1f - (1f - progress) * (1f - progress)
            val r = maxRadius * p.distance * eased
            val angle = p.angle
            val x = cx + r * cos(angle.toDouble()).toFloat()
            val y = cy + r * sin(angle.toDouble()).toFloat()
            drawCircle(
                color = color.copy(alpha = (1f - eased).coerceIn(0f, 1f) * p.alpha),
                radius = p.size * (1f - eased * 0.5f),
                center = Offset(x, y),
            )
        }
    }
}

private data class BurstParticle(val angle: Float, val distance: Float, val size: Float, val alpha: Float)

private fun generateBurstParticles(count: Int): List<BurstParticle> {
    val rng = Random(0xBEEFL)
    return List(count) {
        BurstParticle(
            angle = (rng.nextDouble() * 2 * PI).toFloat(),
            distance = 0.55f + rng.nextFloat() * 0.45f,
            size = 3f + rng.nextFloat() * 5f,
            alpha = 0.7f + rng.nextFloat() * 0.3f,
        )
    }
}
