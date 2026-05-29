package uz.angrykitten.omerta.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.ui.common.ChapterMarker
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing

/**
 * Home Screen — cinematic entrance + glass cards.
 *
 * Composition layers:
 *  1. DepthBackground (multi-layer atmosphere)
 *  2. ParticleField (slow gold embers)
 *  3. Logo with breathing red-glow halo
 *  4. ChapterMarker "00 // Mafia. Roles. Rules."
 *  5. Three action cards with staggered slide-up + scale
 *  6. Settings circle top-right (drifts down on first load)
 *  7. Version footer
 */
@Composable
fun HomeScreen(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    val spacing = LocalSpacing.current
    DepthBackground {
        ParticleField(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondary,
            particleCount = 34,
        )

        var headerVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { delay(60); headerVisible = true }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeTopBar(visible = headerVisible, onSettings = onSettings)
            Spacer(modifier = Modifier.height(spacing.xl))
            HomeLogo(visible = headerVisible)
            Spacer(modifier = Modifier.height(spacing.lg))
            ChapterMarker(
                ordinal = "00",
                label = "MAFIA · ROLES · RULES",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.weight(1f))
            HomeCards(
                onCreateRoom = onCreateRoom,
                onJoinRoom = onJoinRoom,
                onHowToPlay = onHowToPlay,
            )
            Spacer(modifier = Modifier.height(spacing.lg))
            HomeFooter()
            Spacer(modifier = Modifier.height(spacing.sm))
        }
    }
}

@Composable
private fun HomeTopBar(visible: Boolean, onSettings: () -> Unit) {
    val omerta = LocalOmertaColors.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 2 },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(omerta.cardSurface.copy(alpha = 0.6f))
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f), CircleShape)
                    .clickable(role = Role.Button, onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = OmertaIcons.Settings,
                    contentDescription = stringResource(id = R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeLogo(visible: Boolean) {
    val infinite = rememberInfiniteTransition(label = "logo.breathe")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "logo.pulse",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 0.92f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Red glow halo behind the wordmark
            Box(
                modifier = Modifier
                    .size((220 * pulse).dp)
                    .graphicsLayer { alpha = 0.32f }
                    .blur(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.home_logo_word).uppercase(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        fontSize = 58.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.home_logo_tagline).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 8.sp),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HomeCards(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onHowToPlay: () -> Unit,
) {
    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(280)
        cardsVisible = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        StaggeredEntrance(visible = cardsVisible, indexOffsetMs = 0) {
            ActionCard(
                icon = OmertaIcons.Crown,
                title = stringResource(id = R.string.home_card_create_title),
                subtitle = stringResource(id = R.string.home_card_create_subtitle),
                onClick = onCreateRoom,
                iconContentDescription = stringResource(id = R.string.cd_icon_crown),
            )
        }
        StaggeredEntrance(visible = cardsVisible, indexOffsetMs = 80) {
            ActionCard(
                icon = OmertaIcons.Door,
                title = stringResource(id = R.string.home_card_join_title),
                subtitle = stringResource(id = R.string.home_card_join_subtitle),
                onClick = onJoinRoom,
                iconContentDescription = stringResource(id = R.string.cd_icon_door),
            )
        }
        StaggeredEntrance(visible = cardsVisible, indexOffsetMs = 160) {
            ActionCard(
                icon = OmertaIcons.Book,
                title = stringResource(id = R.string.home_card_howto_title),
                subtitle = stringResource(id = R.string.home_card_howto_subtitle),
                onClick = onHowToPlay,
                iconContentDescription = stringResource(id = R.string.cd_icon_book),
            )
        }
    }
}

@Composable
private fun StaggeredEntrance(
    visible: Boolean,
    indexOffsetMs: Int,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(420, delayMillis = indexOffsetMs, easing = LinearOutSlowInEasing),
            initialOffsetY = { it / 3 },
        ) + fadeIn(animationSpec = tween(320, delayMillis = indexOffsetMs))
            + scaleIn(initialScale = 0.96f, animationSpec = tween(420, delayMillis = indexOffsetMs)),
    ) { content() }
}

@Composable
private fun HomeFooter() {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(id = R.string.home_footer_version, versionName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
