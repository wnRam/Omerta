package uz.angrykitten.omerta.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.angrykitten.omerta.OmertaApp
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.data.settings.SettingsRepository
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.ui.common.ChapterMarker
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.common.GlassCard
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory())
    val settings by viewModel.state.collectAsStateWithLifecycle()
    val currentLanguage = LocalAppLanguage.current
    val spacing = LocalSpacing.current

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            OmertaTopBar(
                title = stringResource(id = R.string.settings_title),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                LanguageSection(
                    selected = currentLanguage,
                    onSelect = viewModel::setLanguage,
                )
                ThemeSection(
                    selected = settings.themeMode,
                    onSelect = viewModel::setThemeMode,
                )
                AboutSection()
            }
        }
    }
}

@Composable
private fun LanguageSection(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChapterMarker(ordinal = "01", label = "LANGUAGE", modifier = Modifier.fillMaxWidth())
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageRow(AppLanguage.EN, R.string.language_english, "🇬🇧", selected == AppLanguage.EN, onSelect)
                LanguageRow(AppLanguage.RU, R.string.language_russian, "🇷🇺", selected == AppLanguage.RU, onSelect)
                LanguageRow(AppLanguage.UZ, R.string.language_uzbek, "🇺🇿", selected == AppLanguage.UZ, onSelect)
            }
        }
    }
}

@Composable
private fun LanguageRow(
    lang: AppLanguage,
    labelRes: Int,
    flag: String,
    selected: Boolean,
    onSelect: (AppLanguage) -> Unit,
) {
    val omerta = LocalOmertaColors.current
    val border by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondary else omerta.divider.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "LanguageRow.border",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else omerta.cardSurface.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "LanguageRow.bg",
    )
    val label = stringResource(id = labelRes)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(width = 1.5.dp, color = border, shape = RoundedCornerShape(14.dp))
            .clickable(role = Role.Button) { onSelect(lang) }
            .semantics { contentDescription = label }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = flag, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = OmertaIcons.Checkmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ThemeSection(
    selected: SettingsRepository.ThemeMode,
    onSelect: (SettingsRepository.ThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChapterMarker(ordinal = "02", label = "THEME", modifier = Modifier.fillMaxWidth())
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(
                    label = stringResource(id = R.string.settings_theme_system),
                    selected = selected == SettingsRepository.ThemeMode.SYSTEM,
                    onClick = { onSelect(SettingsRepository.ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f),
                )
                ThemeChip(
                    label = stringResource(id = R.string.settings_theme_light),
                    icon = OmertaIcons.Day,
                    selected = selected == SettingsRepository.ThemeMode.LIGHT,
                    onClick = { onSelect(SettingsRepository.ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                )
                ThemeChip(
                    label = stringResource(id = R.string.settings_theme_dark),
                    icon = OmertaIcons.Night,
                    selected = selected == SettingsRepository.ThemeMode.DARK,
                    onClick = { onSelect(SettingsRepository.ThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val omerta = LocalOmertaColors.current
    val target = if (selected) MaterialTheme.colorScheme.primary else omerta.cardSurface.copy(alpha = 0.5f)
    val bg by animateColorAsState(target, tween(180), label = "ThemeChip.bg")
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = tween(180),
        label = "ThemeChip.border",
    )
    val borderColor = if (selected) MaterialTheme.colorScheme.secondary else omerta.divider.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChapterMarker(ordinal = "03", label = "ABOUT", modifier = Modifier.fillMaxWidth())
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_about_blurb),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.home_footer_version, version),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun settingsViewModelFactory() = viewModelFactory {
    initializer { SettingsViewModel(OmertaApp.get().settingsRepository) }
}
