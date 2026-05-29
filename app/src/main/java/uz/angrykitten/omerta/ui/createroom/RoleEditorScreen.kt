package uz.angrykitten.omerta.ui.createroom

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticRole
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import java.util.UUID
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team
import uz.angrykitten.omerta.domain.model.localizedDescription
import uz.angrykitten.omerta.domain.model.localizedName
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.icons.RoleIconRegistry
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.theme.cardGradient

/**
 * Custom Role Editor. Three localized name+description fields, team picker,
 * curated icon-key grid, live preview. Validates: at least one language must
 * have a non-blank name.
 */
@Composable
fun RoleEditorScreen(
    onBack: () -> Unit,
    onSave: (Role) -> Unit,
) {
    var nameEn by remember { mutableStateOf("") }
    var nameRu by remember { mutableStateOf("") }
    var nameUz by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descRu by remember { mutableStateOf("") }
    var descUz by remember { mutableStateOf("") }
    var team by remember { mutableStateOf(Team.TOWN) }
    var iconKey by remember { mutableStateOf(RoleIconRegistry.allKeys.first()) }

    val spacing = LocalSpacing.current
    val language = LocalAppLanguage.current

    val preview = Role(
        id = "draft",
        nameEn = nameEn, nameRu = nameRu, nameUz = nameUz,
        descriptionEn = descEn, descriptionRu = descRu, descriptionUz = descUz,
        team = team, iconRes = iconKey, isCustom = true,
    )
    val canSave = listOf(nameEn, nameRu, nameUz).any { it.isNotBlank() }

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(
                title = stringResource(id = R.string.role_editor_title),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
            ) {
                LivePreviewCard(preview, language)
                NameFieldsSection(
                    nameEn = nameEn, onNameEn = { nameEn = it },
                    nameRu = nameRu, onNameRu = { nameRu = it },
                    nameUz = nameUz, onNameUz = { nameUz = it },
                )
                DescriptionFieldsSection(
                    descEn = descEn, onDescEn = { descEn = it },
                    descRu = descRu, onDescRu = { descRu = it },
                    descUz = descUz, onDescUz = { descUz = it },
                )
                TeamPickerSection(selected = team, onSelect = { team = it })
                IconPickerSection(selected = iconKey, onSelect = { iconKey = it })
                Spacer(modifier = Modifier.height(spacing.md))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenPadding, vertical = 12.dp),
            ) {
                PrimaryButton(
                    text = stringResource(id = R.string.role_editor_save),
                    enabled = canSave,
                    onClick = {
                        onSave(preview.copy(id = "custom." + UUID.randomUUID().toString().take(8)))
                    },
                )
            }
        }
    }
}

@Composable
private fun LivePreviewCard(role: Role, language: AppLanguage) {
    val omerta = LocalOmertaColors.current
    val accent = when (role.team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardGradient())
            .border(2.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = RoleIconRegistry.forKey(role.iconRes),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            text = role.localizedName(language).ifBlank { stringResource(id = R.string.role_editor_unnamed) },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        val desc = role.localizedDescription(language)
        if (desc.isNotBlank()) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NameFieldsSection(
    nameEn: String, onNameEn: (String) -> Unit,
    nameRu: String, onNameRu: (String) -> Unit,
    nameUz: String, onNameUz: (String) -> Unit,
) {
    SectionContainerLocal(title = stringResource(id = R.string.role_editor_names)) {
        OmertaTextField(label = "EN", value = nameEn, onValueChange = onNameEn)
        OmertaTextField(label = "RU", value = nameRu, onValueChange = onNameRu)
        OmertaTextField(label = "UZ", value = nameUz, onValueChange = onNameUz)
    }
}

@Composable
private fun DescriptionFieldsSection(
    descEn: String, onDescEn: (String) -> Unit,
    descRu: String, onDescRu: (String) -> Unit,
    descUz: String, onDescUz: (String) -> Unit,
) {
    SectionContainerLocal(title = stringResource(id = R.string.role_editor_descriptions)) {
        OmertaTextField(label = "EN", value = descEn, onValueChange = onDescEn, multiline = true)
        OmertaTextField(label = "RU", value = descRu, onValueChange = onDescRu, multiline = true)
        OmertaTextField(label = "UZ", value = descUz, onValueChange = onDescUz, multiline = true)
    }
}

@Composable
private fun OmertaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    multiline: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = if (multiline) 2 else 1,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = LocalOmertaColors.current.divider,
            focusedLabelColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun TeamPickerSection(selected: Team, onSelect: (Team) -> Unit) {
    val omerta = LocalOmertaColors.current
    SectionContainerLocal(title = stringResource(id = R.string.role_editor_team)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TeamChip(Team.TOWN, omerta.teamTown, R.string.team_town, selected == Team.TOWN, onSelect, Modifier.weight(1f))
            TeamChip(Team.MAFIA, omerta.teamMafia, R.string.team_mafia, selected == Team.MAFIA, onSelect, Modifier.weight(1f))
            TeamChip(Team.NEUTRAL, omerta.teamNeutral, R.string.team_neutral, selected == Team.NEUTRAL, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TeamChip(
    team: Team,
    color: Color,
    labelRes: Int,
    selected: Boolean,
    onSelect: (Team) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) color.copy(alpha = 0.85f) else color.copy(alpha = 0.18f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = color,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(role = SemanticRole.Button) { onSelect(team) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (selected) Color.White else color),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IconPickerSection(selected: String, onSelect: (String) -> Unit) {
    SectionContainerLocal(title = stringResource(id = R.string.role_editor_icon)) {
        val keys = RoleIconRegistry.allKeys
        LazyHorizontalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            rows = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(keys.size) { i ->
                val key = keys[i]
                IconChoice(key = key, selected = selected == key, onClick = { onSelect(key) })
            }
        }
    }
}

@Composable
private fun IconChoice(key: String, selected: Boolean, onClick: () -> Unit) {
    val omerta = LocalOmertaColors.current
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else omerta.cardSurface.copy(alpha = 0.55f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.secondary else omerta.divider,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(role = SemanticRole.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = RoleIconRegistry.forKey(key),
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun SectionContainerLocal(title: String, content: @Composable () -> Unit) {
    val omerta = LocalOmertaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardGradient())
            .border(1.dp, omerta.divider, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

