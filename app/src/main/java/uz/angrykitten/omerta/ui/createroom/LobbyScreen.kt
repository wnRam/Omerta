package uz.angrykitten.omerta.ui.createroom

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.common.QrCodeView
import uz.angrykitten.omerta.ui.common.SecondaryButton
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.theme.MonoFamily
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.common.GlassCard

/**
 * Host's lobby. Shows the room code (LAN: also QR), the player roster, the
 * role summary, and the Start Game button.
 *
 * Local-mode lobby is a degenerate case: just shows the seat-count and lets
 * the host go straight to Role Reveal. There's no room code/QR because
 * everyone shares one device.
 */
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onBack: () -> Unit,
    onStartReveal: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    var kickTarget by remember { mutableStateOf<Player?>(null) }

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(
                title = stringResource(id = R.string.lobby_title),
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
            ) {
                if (state.mode == GameMode.LAN) {
                    item { RoomCodeCard(code = state.roomCode, onCopy = { copyToClipboard(context, state.roomCode) }, onShare = { shareCode(context, state.roomCode) }) }
                    item { QrCard(code = state.roomCode) }
                }
                item { RoleSummaryCard(state.roleSummary) }
                item { PlayersCard(state.players, onKick = { kickTarget = it }) }
            }
            BottomLobbyActions(
                playerCount = state.players.size,
                minPlayers = state.minPlayers,
                onStart = onStartReveal,
            )
        }
        if (kickTarget != null) {
            KickConfirmDialog(
                playerName = kickTarget!!.name,
                onConfirm = {
                    viewModel.kickPlayer(kickTarget!!.id); kickTarget = null
                },
                onDismiss = { kickTarget = null },
            )
        }
    }
}

@Composable
private fun RoomCodeCard(code: String, onCopy: () -> Unit, onShare: () -> Unit) {
    SectionCardLocal(title = stringResource(id = R.string.lobby_room_code)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = code,
                fontFamily = MonoFamily,
                fontSize = 36.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = stringResource(id = R.string.lobby_copy),
                    onClick = onCopy,
                    leadingIcon = OmertaIcons.Copy,
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = stringResource(id = R.string.lobby_share),
                    onClick = onShare,
                    leadingIcon = OmertaIcons.Share,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QrCard(code: String) {
    SectionCardLocal(title = stringResource(id = R.string.lobby_qr)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
            ) {
                QrCodeView(
                    content = "omerta://$code",
                    size = 196.dp,
                    foreground = Color(0xFF0A0A0F),
                    background = Color.White,
                )
            }
        }
    }
}

@Composable
private fun RoleSummaryCard(summary: List<Pair<String, Int>>) {
    SectionCardLocal(title = stringResource(id = R.string.lobby_roles_in_play)) {
        if (summary.isEmpty()) {
            Text(
                text = stringResource(id = R.string.lobby_no_roles),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.forEach { (roleName, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roleName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "×$count",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayersCard(players: List<Player>, onKick: (Player) -> Unit) {
    SectionCardLocal(title = stringResource(id = R.string.lobby_players, players.size)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            players.forEach { player ->
                PlayerRow(player = player, onKick = { onKick(player) })
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player, onKick: () -> Unit) {
    val omerta = LocalOmertaColors.current
    val initial = player.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(omerta.cardSurface.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (player.isHost) {
            Icon(
                imageVector = OmertaIcons.Crown,
                contentDescription = stringResource(id = R.string.cd_icon_crown),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Icon(
                imageVector = OmertaIcons.Close,
                contentDescription = stringResource(id = R.string.lobby_kick),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onKick),
            )
        }
    }
}

@Composable
private fun BottomLobbyActions(playerCount: Int, minPlayers: Int, onStart: () -> Unit) {
    val spacing = LocalSpacing.current
    val canStart = playerCount >= minPlayers
    val short = (minPlayers - playerCount).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacing.screenPadding, vertical = 12.dp),
    ) {
        PrimaryButton(
            text = if (canStart) stringResource(id = R.string.lobby_start)
            else stringResource(id = R.string.lobby_waiting_for_players, short),
            enabled = canStart,
            onClick = onStart,
        )
    }
}

@Composable
private fun KickConfirmDialog(playerName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.lobby_kick_title), color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = stringResource(id = R.string.lobby_kick_body, playerName), color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.lobby_kick), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = LocalOmertaColors.current.cardSurface,
    )
}

@Composable
private fun SectionCardLocal(title: String, content: @Composable () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 2.5.sp),
                color = MaterialTheme.colorScheme.secondary,
            )
            content()
        }
    }
}

private fun copyToClipboard(context: Context, code: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Omerta room code", code))
}

private fun shareCode(context: Context, code: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "omerta://$code")
    }
    context.startActivity(Intent.createChooser(send, null))
}
