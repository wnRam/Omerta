package uz.angrykitten.omerta.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.angrykitten.omerta.OmertaApp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.domain.model.Team
import uz.angrykitten.omerta.domain.model.localizedName
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.common.SecondaryButton
import uz.angrykitten.omerta.ui.home.ParticleField
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.icons.RoleIconRegistry
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing

@Composable
fun EndGameScreen(
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val session = remember { OmertaApp.get().gameSession }
    val state by session.state.collectAsStateWithLifecycle()
    val omerta = LocalOmertaColors.current
    val spacing = LocalSpacing.current
    val language = LocalAppLanguage.current

    // FIXED: guard against process-death recreation — if the system killed
    // the process and the user re-opens the app, GameSession is blank.
    // Navigate home rather than showing an empty results screen.
    if (state.players.isEmpty()) {
        LaunchedEffect(Unit) { onHome() }
        return
    }

    val team = state.winningTeam
    val accent = when (team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
        null -> MaterialTheme.colorScheme.secondary
    }
    val bg = Brush.verticalGradient(listOf(accent.copy(alpha = 0.55f), omerta.cardSurfaceBottom, MaterialTheme.colorScheme.background))

    var revealAll by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(450); revealAll = true }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        ParticleField(
            modifier = Modifier.fillMaxSize(),
            color = if (team == Team.MAFIA) Color(0xFFE74C3C) else Color(0xFFF1C40F),
            particleCount = 36,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            VictoryBanner(team = team)
            Spacer(modifier = Modifier.height(20.dp))
            StatsRow(state.round, state.players)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.endgame_reveal_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(state.players) { i, player ->
                    AnimatedVisibility(
                        visible = revealAll,
                        enter = slideInVertically(
                            animationSpec = tween(400, delayMillis = i * 60),
                            initialOffsetY = { it / 2 },
                        ) + fadeIn(tween(300, delayMillis = i * 60)),
                    ) {
                        PlayerRevealRow(player, language)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = stringResource(id = R.string.endgame_home),
                    onClick = onHome,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = stringResource(id = R.string.endgame_replay),
                    onClick = onPlayAgain,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun VictoryBanner(team: Team?) {
    val labelRes = when (team) {
        Team.MAFIA -> R.string.endgame_mafia_wins
        Team.TOWN -> R.string.endgame_town_wins
        Team.NEUTRAL -> R.string.endgame_neutral_wins
        null -> R.string.endgame_undecided
    }
    Text(
        text = stringResource(id = labelRes),
        style = MaterialTheme.typography.displayMedium,
        color = Color.White,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StatsRow(round: Int, players: List<Player>) {
    val omerta = LocalOmertaColors.current
    val eliminated = players.count { it.isEliminated }
    val mafia = players.count { it.assignedRole?.team == Team.MAFIA }
    val town = players.count { it.assignedRole?.team == Team.TOWN }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        StatPill(stringResource(id = R.string.endgame_stat_rounds, round))
        StatPill(stringResource(id = R.string.endgame_stat_elim, eliminated))
        StatPill(
            stringResource(
                id = if (mafia > town) R.string.endgame_stat_majority_mafia
                else R.string.endgame_stat_majority_town,
            ),
        )
    }
}

@Composable
private fun StatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun PlayerRevealRow(player: Player, language: uz.angrykitten.omerta.domain.model.AppLanguage) {
    val role = player.assignedRole
    val omerta = LocalOmertaColors.current
    val accent = when (role?.team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
        null -> omerta.cardSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            if (role != null) {
                Icon(
                    imageVector = RoleIconRegistry.forKey(role.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(
                    imageVector = OmertaIcons.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textDecoration = if (player.isEliminated) TextDecoration.LineThrough else null,
            )
            Text(
                text = role?.localizedName(language) ?: stringResource(id = R.string.endgame_role_unknown),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

