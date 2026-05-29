package uz.angrykitten.omerta.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors

/**
 * Themed top app bar. Material3's TopAppBar pulls Material design tonal
 * surfaces that fight the parchment/noir aesthetic, so we draw our own.
 *
 * Auto-applies statusBarsPadding so callers don't have to think about insets.
 * Renders the divider hairline as part of the bar so all screens get the
 * same look.
 */
@Composable
fun OmertaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val omerta = LocalOmertaColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onBack != null) {
                CircleIconButton(
                    onClick = onBack,
                    description = stringResource(id = R.string.action_back),
                ) {
                    Icon(
                        imageVector = OmertaIcons.Back,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            trailing?.invoke()
            Spacer(modifier = Modifier.width(if (onBack == null) 8.dp else 0.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(omerta.divider.copy(alpha = 0.4f)),
        )
    }
}

// AUDIT FIX: parameter renamed from `contentDescription` to `description` —
// previously shadowed the imported `androidx.compose.ui.semantics.contentDescription`
// extension property inside the `semantics { ... }` lambda. Kotlin usually picks
// the outer-scope local over the receiver member, but the visual collision was
// confusing and on some Kotlin versions triggers resolution errors.
@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    background: Color = LocalOmertaColors.current.cardSurface.copy(alpha = 0.6f),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
