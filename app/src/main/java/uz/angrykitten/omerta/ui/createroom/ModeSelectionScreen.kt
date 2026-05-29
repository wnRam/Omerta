package uz.angrykitten.omerta.ui.createroom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.theme.cardGradient

@Composable
fun ModeSelectionScreen(
    onBack: () -> Unit,
    onModeChosen: (GameMode) -> Unit,
) {
    var selected by remember { mutableStateOf<GameMode?>(null) }
    val spacing = LocalSpacing.current

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(
                title = stringResource(id = R.string.create_room_title),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = stringResource(id = R.string.mode_pick_heading),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(id = R.string.mode_pick_subhead),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                ModeCard(
                    icon = OmertaIcons.Door,
                    titleRes = R.string.mode_local_title,
                    descriptionRes = R.string.mode_local_desc,
                    bestForRes = R.string.mode_local_best_for,
                    selected = selected == GameMode.LOCAL,
                    onClick = { selected = GameMode.LOCAL },
                )
                ModeCard(
                    icon = OmertaIcons.Share,
                    titleRes = R.string.mode_lan_title,
                    descriptionRes = R.string.mode_lan_desc,
                    bestForRes = R.string.mode_lan_best_for,
                    selected = selected == GameMode.LAN,
                    onClick = { selected = GameMode.LAN },
                )
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = stringResource(id = R.string.action_continue),
                    enabled = selected != null,
                    onClick = { selected?.let(onModeChosen) },
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    titleRes: Int,
    descriptionRes: Int,
    bestForRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val omerta = LocalOmertaColors.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = tween(180),
        label = "ModeCard.scale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondary else omerta.divider,
        animationSpec = tween(180),
        label = "ModeCard.border",
    )
    val borderWidth = if (selected) 2.dp else 1.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(cardGradient())
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = stringResource(id = descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(id = bestForRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
