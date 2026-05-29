package uz.angrykitten.omerta.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.angrykitten.omerta.OmertaApp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.game.GameSession
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.ui.common.SecondaryButton
import uz.angrykitten.omerta.ui.home.ParticleField
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing

/**
 * Phase tracker. Animated per-phase background (Night/Day/Voting), circular
 * Canvas timer, host control panel (toggled via FAB), collapsible player
 * list bottom sheet.
 */
@Composable
fun PhaseTrackerScreen(
    onBack: () -> Unit,
    onGameEnd: () -> Unit,
) {
    val session = remember { OmertaApp.get().gameSession }
    val state by session.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    // FIXED: guard against process-death recreation — if the process was
    // killed, GameSession is blank. Navigate home rather than rendering
    // a broken phase tracker with no players.
    if (state.players.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // FIXED: suspend when paused instead of spinning the while-loop
    // every second. The old version called tick() which early-returned,
    // but still woke the CPU once per second — bad for battery life.
    LaunchedEffect(state.phase, state.timerPaused, state.secondsRemaining == null) {
        if (state.timerPaused || state.secondsRemaining == null) return@LaunchedEffect
        while (true) {
            delay(1000)
            session.tick()
        }
    }

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.ENDED) onGameEnd()
    }

    var hostControlsOpen by remember { mutableStateOf(false) }
    var bottomSheetOpen by remember { mutableStateOf(false) }
    var endGameDialog by remember { mutableStateOf(false) }
    // ADDED: intercept system back press to prevent accidental game loss.
    var backConfirmDialog by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler { backConfirmDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        PhaseBackground(phase = state.phase)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = spacing.screenPadding, vertical = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhaseHeader(phase = state.phase, round = state.round)
            Spacer(modifier = Modifier.weight(1f))
            CountdownTimer(
                secondsRemaining = state.secondsRemaining,
                paused = state.timerPaused,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = phaseSubtitle(state.phase),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SecondaryButton(
                    text = stringResource(id = R.string.phase_players_button, state.players.count { !it.isEliminated }, state.players.size),
                    onClick = { bottomSheetOpen = true },
                    modifier = Modifier.weight(1f),
                    leadingIcon = OmertaIcons.Eye,
                )
                SecondaryButton(
                    text = stringResource(id = R.string.phase_host_controls),
                    onClick = { hostControlsOpen = !hostControlsOpen },
                    modifier = Modifier.weight(1f),
                    leadingIcon = OmertaIcons.Crown,
                )
            }
            AnimatedVisibility(visible = hostControlsOpen) {
                HostControlPanel(
                    paused = state.timerPaused,
                    onPauseToggle = { session.setPaused(!state.timerPaused) },
                    onSkip = { session.advancePhase() },
                    onExtend30 = { session.extendTimer(30) },
                    onExtend60 = { session.extendTimer(60) },
                    onEndGame = { endGameDialog = true },
                )
            }
        }

        if (bottomSheetOpen) {
            PlayerSheet(
                players = state.players,
                onDismiss = { bottomSheetOpen = false },
                onToggleEliminated = { session.toggleEliminated(it.id) },
            )
        }

        if (endGameDialog) {
            EndGameConfirmDialog(
                onConfirm = {
                    session.endGame()
                    endGameDialog = false
                },
                onDismiss = { endGameDialog = false },
            )
        }

        // ADDED: back-press confirmation so an accidental swipe doesn't
        // silently destroy the active game session.
        if (backConfirmDialog) {
            LeaveGameConfirmDialog(
                onConfirm = {
                    backConfirmDialog = false
                    onBack()
                },
                onDismiss = { backConfirmDialog = false },
            )
        }
    }
}

@Composable
private fun PhaseBackground(phase: GamePhase) {
    val omerta = LocalOmertaColors.current
    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
        label = "phase.background",
    ) { p ->
        when (p) {
            GamePhase.NIGHT -> NightBackground()
            GamePhase.DAY -> DayBackground()
            GamePhase.VOTING -> VotingBackground()
            else -> Box(modifier = Modifier.fillMaxSize().background(omerta.cardSurfaceBottom))
        }
    }
}

@Composable
private fun NightBackground() {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF050514), Color(0xFF0A0A1E), Color(0xFF050514)))
    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        ParticleField(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF6FA0FF),
            particleCount = 40,
        )
        Icon(
            imageVector = OmertaIcons.Night,
            contentDescription = null,
            tint = Color(0xFFFFF5C0).copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 28.dp)
                .size(48.dp),
        )
    }
}

@Composable
private fun DayBackground() {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFFFFD78F), Color(0xFFF1A661), Color(0xFF8B4513).copy(alpha = 0.4f)),
    )
    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        Icon(
            imageVector = OmertaIcons.Day,
            contentDescription = null,
            tint = Color(0xFFFFFFFF),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .size(64.dp)
                .alpha(0.85f),
        )
    }
}

@Composable
private fun VotingBackground() {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF6B1A14), Color(0xFF3D0E0A), Color(0xFF1A0504)),
    )
    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        ParticleField(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF1C40F),
            particleCount = 18,
        )
    }
}

@Composable
private fun PhaseHeader(phase: GamePhase, round: Int) {
    val (iconRes, labelRes) = when (phase) {
        GamePhase.NIGHT -> OmertaIcons.Night to R.string.phase_night
        GamePhase.DAY -> OmertaIcons.Day to R.string.phase_day
        GamePhase.VOTING -> OmertaIcons.Vote to R.string.phase_voting
        else -> OmertaIcons.Timer to R.string.phase_other
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.phase_round, round),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconRes,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = labelRes),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun phaseSubtitle(phase: GamePhase): String = stringResource(
    id = when (phase) {
        GamePhase.NIGHT -> R.string.phase_night_sub
        GamePhase.DAY -> R.string.phase_day_sub
        GamePhase.VOTING -> R.string.phase_voting_sub
        else -> R.string.phase_other_sub
    },
)

@Composable
private fun CountdownTimer(secondsRemaining: Int?, paused: Boolean) {
    if (secondsRemaining == null) {
        Text(
            text = "∞",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
            color = Color.White,
        )
        return
    }
    val totalSecondsKey = remember(secondsRemaining > 0) { secondsRemaining.coerceAtLeast(1) }
    val animated by animateFloatAsState(
        targetValue = secondsRemaining.toFloat() / totalSecondsKey.toFloat(),
        animationSpec = tween(500),
        label = "timer.ring",
    )
    val isCritical = secondsRemaining <= 10
    val ringColor = if (isCritical) Color(0xFFE74C3C) else Color(0xFFF1C40F)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.55f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14f
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animated.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(secondsRemaining),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            if (paused) {
                Text(
                    text = stringResource(id = R.string.phase_paused),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun HostControlPanel(
    paused: Boolean,
    onPauseToggle: () -> Unit,
    onSkip: () -> Unit,
    onExtend30: () -> Unit,
    onExtend60: () -> Unit,
    onEndGame: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(
                text = stringResource(id = if (paused) R.string.phase_resume else R.string.phase_pause),
                onClick = onPauseToggle,
                leadingIcon = if (paused) OmertaIcons.Play else OmertaIcons.Pause,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = stringResource(id = R.string.phase_skip),
                onClick = onSkip,
                leadingIcon = OmertaIcons.Skip,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(
                text = "+30s",
                onClick = onExtend30,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = "+60s",
                onClick = onExtend60,
                modifier = Modifier.weight(1f),
            )
        }
        SecondaryButton(
            text = stringResource(id = R.string.phase_end_game),
            onClick = onEndGame,
            leadingIcon = OmertaIcons.Warning,
            accent = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PlayerSheet(
    players: List<Player>,
    onDismiss: () -> Unit,
    onToggleEliminated: (Player) -> Unit,
) {
    // Custom bottom sheet — Material3 ModalBottomSheet would work too but the
    // noir aesthetic prefers a non-elevated container with a dim scrim.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(LocalOmertaColors.current.cardSurface)
                .clickable(enabled = false, onClick = {})
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val alive = players.count { !it.isEliminated }
            Text(
                text = stringResource(id = R.string.phase_alive_count, alive, players.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            players.forEach { player ->
                PlayerSheetRow(player = player, onToggle = { onToggleEliminated(player) })
            }
        }
    }
}

@Composable
private fun PlayerSheetRow(player: Player, onToggle: () -> Unit) {
    val omerta = LocalOmertaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(omerta.cardSurfaceBottom)
            .clickable(role = SemanticRole.Button, onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (player.isEliminated) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else omerta.success.copy(alpha = 0.7f),
                ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textDecoration = if (player.isEliminated) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                id = if (player.isEliminated) R.string.phase_player_eliminated
                else R.string.phase_player_alive,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = if (player.isEliminated) MaterialTheme.colorScheme.error else omerta.success,
        )
    }
}

@Composable
private fun EndGameConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalOmertaColors.current.cardSurface,
        title = { Text(text = stringResource(id = R.string.phase_end_confirm_title), color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = stringResource(id = R.string.phase_end_confirm_body), color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.action_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

// ADDED: confirmation dialog for system back press during an active game.
@Composable
private fun LeaveGameConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalOmertaColors.current.cardSurface,
        title = { Text(text = stringResource(id = R.string.phase_leave_confirm_title), color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = stringResource(id = R.string.phase_leave_confirm_body), color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.action_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}
