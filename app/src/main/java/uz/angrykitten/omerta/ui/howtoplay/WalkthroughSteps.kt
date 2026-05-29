package uz.angrykitten.omerta.ui.howtoplay

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.MonoFamily

/**
 * Seven-step app guide. Each step shows a "phone-in-a-phone" frame with a
 * mini-mockup of the relevant screen, plus narration.
 *
 * The mini-mockup uses simplified token UI (we re-render shrunken cards
 * rather than embedding the real screens — embedding the real screens
 * inside themselves would create infinite recursion via the phone frame's
 * own Compose tree).
 */
sealed class WalkthroughStep {
    @Composable
    abstract fun Render()

    object Create : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_create_title,
            bodyRes = R.string.guide_create_body,
            mock = { MockHome() },
        )
    }

    object PickMode : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_mode_title,
            bodyRes = R.string.guide_mode_body,
            mock = { MockModePick() },
        )
    }

    object SetupRoles : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_roles_title,
            bodyRes = R.string.guide_roles_body,
            mock = { MockRoles() },
        )
    }

    object ShareCode : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_code_title,
            bodyRes = R.string.guide_code_body,
            mock = { MockCodeShare() },
        )
    }

    object PlayersJoin : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_join_title,
            bodyRes = R.string.guide_join_body,
            mock = { MockPlayersJoin() },
        )
    }

    object Reveal : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_reveal_title,
            bodyRes = R.string.guide_reveal_body,
            mock = { MockReveal() },
        )
    }

    object TrackGame : WalkthroughStep() {
        @Composable
        override fun Render() = StepLayout(
            titleRes = R.string.guide_track_title,
            bodyRes = R.string.guide_track_body,
            mock = { MockPhase() },
        )
    }
}

object WalkthroughSteps {
    val list: List<WalkthroughStep> = listOf(
        WalkthroughStep.Create,
        WalkthroughStep.PickMode,
        WalkthroughStep.SetupRoles,
        WalkthroughStep.ShareCode,
        WalkthroughStep.PlayersJoin,
        WalkthroughStep.Reveal,
        WalkthroughStep.TrackGame,
    )
}

@Composable
private fun StepLayout(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    mock: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PhoneFrame(content = mock)
        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PhoneFrame(content: @Composable () -> Unit) {
    val omerta = LocalOmertaColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(0.5f)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1A1A20))
            .border(width = 6.dp, color = Color(0xFF0A0A10), shape = RoundedCornerShape(28.dp))
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(omerta.cardSurfaceBottom),
        ) { content() }
    }
}

@Composable
private fun MockHome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "OMERTA",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        MockCard(label = "Create Room", icon = OmertaIcons.Crown, highlighted = true)
        MockCard(label = "Join Room", icon = OmertaIcons.Door)
        MockCard(label = "How to Play", icon = OmertaIcons.Book)
    }
}

@Composable
private fun MockModePick() {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        MockCard(label = "Local Mode", icon = OmertaIcons.Door, highlighted = true)
        MockCard(label = "LAN Mode", icon = OmertaIcons.Share)
    }
}

@Composable
private fun MockRoles() {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf("Citizen ×3", "Mafia ×2", "Sheriff ×1", "Doctor ×1").forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LocalOmertaColors.current.cardSurface)
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun MockCodeShare() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "WOLF42",
            fontFamily = MonoFamily,
            fontSize = 22.sp,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White)
                .padding(4.dp),
        ) {
            // Tiny QR mock — random-looking checker pattern
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cell = size.width / 8f
                for (y in 0 until 8) for (x in 0 until 8) {
                    if ((x * 7 + y * 11 + 3) % 3 == 0) {
                        drawRect(
                            color = Color.Black,
                            topLeft = androidx.compose.ui.geometry.Offset(x * cell, y * cell),
                            size = androidx.compose.ui.geometry.Size(cell, cell),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockPlayersJoin() {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf("Anna", "Boris", "Carl", "Dina").forEach { name ->
            MockCard(label = name, icon = OmertaIcons.Crown)
        }
    }
}

@Composable
private fun MockReveal() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LocalOmertaColors.current.teamMafia.copy(alpha = 0.8f))
                .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = uz.angrykitten.omerta.ui.icons.RoleIcons.Mafia,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

@Composable
private fun MockPhase() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = OmertaIcons.Night,
            contentDescription = null,
            tint = Color(0xFFFFF5C0),
            modifier = Modifier.size(36.dp),
        )
        Text(text = "0:45", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MockCard(label: String, icon: ImageVector, highlighted: Boolean = false) {
    val omerta = LocalOmertaColors.current
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else omerta.cardSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
