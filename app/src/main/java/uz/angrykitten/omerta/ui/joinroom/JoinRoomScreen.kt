package uz.angrykitten.omerta.ui.joinroom

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticRole
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.game.RoomCode
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.theme.MonoFamily
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.theme.cardGradient

@Composable
fun JoinRoomScreen(
    onBack: () -> Unit,
    onJoinByCode: (String) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(JoinTab.CODE) }
    val spacing = LocalSpacing.current

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(title = stringResource(id = R.string.join_room_title), onBack = onBack)
            TabBar(selected = selectedTab, onSelect = { selectedTab = it })
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenPadding),
                label = "join.tab",
            ) { tab ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (tab) {
                        JoinTab.CODE -> CodeEntryPanel(onJoin = onJoinByCode)
                        // AUDIT FIX: scanner is now inline — tapping the QR tab
                        // requests camera permission and shows the camera preview
                        // immediately on grant, no separate route + no crash.
                        JoinTab.QR -> InlineQrScan(onCodeReceived = onJoinByCode)
                    }
                }
            }
        }
    }
}

private enum class JoinTab { CODE, QR }

@Composable
private fun TabBar(selected: JoinTab, onSelect: (JoinTab) -> Unit) {
    val omerta = LocalOmertaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(omerta.cardSurface.copy(alpha = 0.55f))
            .padding(4.dp),
    ) {
        TabChip(
            label = stringResource(id = R.string.join_tab_code),
            icon = OmertaIcons.Lock,
            selected = selected == JoinTab.CODE,
            onClick = { onSelect(JoinTab.CODE) },
            modifier = Modifier.weight(1f),
        )
        TabChip(
            label = stringResource(id = R.string.join_tab_qr),
            icon = OmertaIcons.QrScan,
            selected = selected == JoinTab.QR,
            onClick = { onSelect(JoinTab.QR) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(role = SemanticRole.Tab, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CodeEntryPanel(onJoin: (String) -> Unit) {
    var raw by remember { mutableStateOf("") }
    val normalized = RoomCode.normalize(raw)
    val valid = RoomCode.isValid(normalized)
    var attemptedInvalid by remember { mutableStateOf(false) }
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(attemptedInvalid) {
        if (attemptedInvalid) {
            shakeAnim.snapTo(0f)
            listOf(-12f, 12f, -8f, 8f, -4f, 4f, 0f).forEach {
                shakeAnim.animateTo(it, animationSpec = tween(60))
            }
            attemptedInvalid = false
        }
    }
    val omerta = LocalOmertaColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.join_code_prompt),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = shakeAnim.value }
                .clip(RoundedCornerShape(16.dp))
                .background(cardGradient())
                .border(1.5.dp, omerta.divider, RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = normalized,
                onValueChange = { raw = it },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                textStyle = TextStyle(
                    fontFamily = MonoFamily,
                    fontSize = 32.sp,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(id = R.string.join_action),
            enabled = valid,
            onClick = {
                if (valid) onJoin(normalized) else attemptedInvalid = true
            },
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun InlineQrScan(onCodeReceived: (String) -> Unit) {
    val permission = rememberPermissionState(Manifest.permission.CAMERA)
    val omerta = LocalOmertaColors.current

    when (val status = permission.status) {
        PermissionStatus.Granted -> InlineCameraFrame(onCodeReceived = onCodeReceived)
        is PermissionStatus.Denied -> Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = OmertaIcons.QrScan,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp),
                )
            }
            Text(
                text = stringResource(
                    id = if (status.shouldShowRationale) R.string.camera_perm_rationale
                    else R.string.camera_perm_request,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                text = stringResource(id = R.string.camera_perm_grant),
                onClick = { permission.launchPermissionRequest() },
            )
        }
    }
}

@Composable
private fun InlineCameraFrame(onCodeReceived: (String) -> Unit) {
    val omerta = LocalOmertaColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, omerta.divider, RoundedCornerShape(24.dp))
                .background(Color.Black),
        ) {
            QrScannerView(
                onDetected = { raw ->
                    val payload = raw.removePrefix("omerta://").trim()
                    val code = RoomCode.normalize(payload)
                    if (RoomCode.isValid(code)) onCodeReceived(code)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = stringResource(id = R.string.join_qr_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
