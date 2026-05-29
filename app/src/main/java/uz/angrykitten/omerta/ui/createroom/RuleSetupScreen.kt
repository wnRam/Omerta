package uz.angrykitten.omerta.ui.createroom

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.domain.catalog.DefaultRoles
import uz.angrykitten.omerta.domain.game.RoomConfig
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team
import uz.angrykitten.omerta.domain.model.localizedDescription
import uz.angrykitten.omerta.domain.model.localizedName
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.common.PrimaryButton
import uz.angrykitten.omerta.ui.icons.OmertaIcons
import uz.angrykitten.omerta.ui.icons.RoleIconRegistry
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.theme.LocalSpacing
import uz.angrykitten.omerta.ui.common.ChapterMarker
import uz.angrykitten.omerta.ui.common.DepthBackground
import uz.angrykitten.omerta.ui.common.GlassCard

@Composable
fun RuleSetupScreen(
    viewModel: CreateRoomViewModel,
    onBack: () -> Unit,
    onOpenRoleEditor: () -> Unit,
    onStart: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val language = LocalAppLanguage.current

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(
                title = stringResource(id = R.string.rule_setup_title),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenPadding, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
            ) {
                SeatsHeader(state.totalSeats, state.canStart, state.seatsShortBy)
                RoleGroupSection(
                    titleRes = R.string.roles_classic,
                    roles = DefaultRoles.classic,
                    counts = state.roleCounts,
                    language = language,
                    onAdjust = viewModel::adjustRole,
                )
                RoleGroupSection(
                    titleRes = R.string.roles_extended,
                    roles = DefaultRoles.extended,
                    counts = state.roleCounts,
                    language = language,
                    onAdjust = viewModel::adjustRole,
                )
                CustomRolesSection(
                    customs = state.customRoles,
                    counts = state.roleCounts,
                    language = language,
                    onAdjust = viewModel::adjustRole,
                    onDelete = viewModel::deleteCustomRole,
                    onAddCustom = onOpenRoleEditor,
                )
                if (state.mode == GameMode.LAN) {
                    PhaseTimersSection(state.phaseTimers, viewModel::setPhaseTimer)
                }
                HostOptionsSection(
                    hostSeesRoles = state.hostSeesRoles,
                    allowSkipReveal = state.allowSkipReveal,
                    onHostSeesRoles = viewModel::setHostSeesRoles,
                    onAllowSkipReveal = viewModel::setAllowSkipReveal,
                )
                Spacer(modifier = Modifier.height(spacing.md))
            }
            BottomActions(
                canStart = state.canStart,
                seatsShortBy = state.seatsShortBy,
                onStart = onStart,
            )
        }
    }
}



@Composable
private fun SeatsHeader(total: Int, canStart: Boolean, shortBy: Int) {
    val omerta = LocalOmertaColors.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChapterMarker(ordinal = "00", label = "ROUND SETUP", modifier = Modifier.fillMaxWidth())
            Text(
                text = stringResource(id = R.string.rule_seats_count, total),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = if (canStart) stringResource(id = R.string.rule_seats_ready)
                else stringResource(id = R.string.rule_seats_short, shortBy),
                style = MaterialTheme.typography.bodyMedium,
                color = if (canStart) omerta.success else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun RoleGroupSection(
    titleRes: Int,
    roles: List<Role>,
    counts: Map<String, Int>,
    language: AppLanguage,
    onAdjust: (String, Int) -> Unit,
) {
    SectionContainer(title = stringResource(id = titleRes)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            roles.forEach { role ->
                RoleStepperRow(
                    role = role,
                    count = counts[role.id] ?: 0,
                    language = language,
                    onAdjust = onAdjust,
                )
            }
        }
    }
}

@Composable
private fun CustomRolesSection(
    customs: List<Role>,
    counts: Map<String, Int>,
    language: AppLanguage,
    onAdjust: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onAddCustom: () -> Unit,
) {
    SectionContainer(title = stringResource(id = R.string.roles_custom)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (customs.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.roles_custom_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                customs.forEach { role ->
                    RoleStepperRow(
                        role = role,
                        count = counts[role.id] ?: 0,
                        language = language,
                        onAdjust = onAdjust,
                        trailing = {
                            Icon(
                                imageVector = OmertaIcons.Trash,
                                contentDescription = stringResource(id = R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(role.id) },
                            )
                        },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(role = SemanticRole.Button, onClick = onAddCustom)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = OmertaIcons.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.roles_custom_add),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun RoleStepperRow(
    role: Role,
    count: Int,
    language: AppLanguage,
    onAdjust: (String, Int) -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    val omerta = LocalOmertaColors.current
    var expanded by remember(role.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(omerta.cardSurface.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded },
        ) {
            Icon(
                imageVector = RoleIconRegistry.forKey(role.iconRes),
                contentDescription = null,
                tint = teamColor(role.team),
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = role.localizedName(language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TeamDot(color = teamColor(role.team))
            Spacer(modifier = Modifier.width(10.dp))
            StepperControl(count = count, onAdjust = { onAdjust(role.id, it) })
            if (trailing != null) {
                Spacer(modifier = Modifier.width(10.dp))
                trailing()
            }
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = role.localizedDescription(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StepperControl(count: Int, onAdjust: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // IMPROVED: pass content descriptions for accessibility (TalkBack).
        StepperBtn(icon = OmertaIcons.Remove, enabled = count > 0, description = "Decrease count") { onAdjust(-1) }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .width(28.dp)
                .padding(horizontal = 4.dp),
        )
        StepperBtn(icon = OmertaIcons.Add, enabled = true, description = "Increase count") { onAdjust(1) }
    }
}

@Composable
private fun StepperBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    description: String, // ADDED: accessibility label for screen readers
    onClick: () -> Unit,
) {
    val omerta = LocalOmertaColors.current
    val tint = if (enabled) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(omerta.cardSurface)
            .clickable(enabled = enabled, role = SemanticRole.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // FIXED: was contentDescription = null — now announces the action.
        Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TeamDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            // ADDED: mark as decorative — TalkBack skips it, which is
            // correct because the team name is rendered as text nearby.
            .semantics { contentDescription = "" },
    )
}

@Composable
private fun teamColor(team: Team): Color {
    val omerta = LocalOmertaColors.current
    return when (team) {
        Team.MAFIA -> omerta.teamMafia
        Team.TOWN -> omerta.teamTown
        Team.NEUTRAL -> omerta.teamNeutral
    }
}

@Composable
private fun PhaseTimersSection(
    timers: Map<GamePhase, Int?>,
    onTimerChange: (GamePhase, Int?) -> Unit,
) {
    SectionContainer(title = stringResource(id = R.string.phase_timers_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PhaseTimerRow(GamePhase.NIGHT, R.string.phase_night, timers[GamePhase.NIGHT], onTimerChange)
            PhaseTimerRow(GamePhase.DAY, R.string.phase_day, timers[GamePhase.DAY], onTimerChange)
            PhaseTimerRow(GamePhase.VOTING, R.string.phase_voting, timers[GamePhase.VOTING], onTimerChange)
        }
    }
}

@Composable
private fun PhaseTimerRow(
    phase: GamePhase,
    @androidx.annotation.StringRes labelRes: Int,
    seconds: Int?,
    onTimerChange: (GamePhase, Int?) -> Unit,
) {
    val range = RoomConfig.PHASE_TIMER_RANGES[phase] ?: 30..120
    val infinite = seconds == null
    val effective = seconds ?: range.first
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (infinite) stringResource(id = R.string.phase_timer_infinite)
                else stringResource(id = R.string.phase_timer_seconds, effective),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = !infinite,
                onCheckedChange = { enabled ->
                    onTimerChange(phase, if (enabled) effective else null)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        Slider(
            value = effective.toFloat(),
            onValueChange = { onTimerChange(phase, it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            enabled = !infinite,
            steps = (range.last - range.first) / 15 - 1,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun HostOptionsSection(
    hostSeesRoles: Boolean,
    allowSkipReveal: Boolean,
    onHostSeesRoles: (Boolean) -> Unit,
    onAllowSkipReveal: (Boolean) -> Unit,
) {
    SectionContainer(title = stringResource(id = R.string.host_options_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleRow(
                label = stringResource(id = R.string.host_option_referee),
                checked = hostSeesRoles,
                onChange = onHostSeesRoles,
            )
            ToggleRow(
                label = stringResource(id = R.string.host_option_skip_reveal),
                checked = allowSkipReveal,
                onChange = onAllowSkipReveal,
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun SectionContainer(title: String, content: @Composable () -> Unit) {
    // AUDIT FIX: lifted the panel onto the new GlassCard so every section
    // matches the home/settings visual language.
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

@Composable
private fun BottomActions(canStart: Boolean, seatsShortBy: Int, onStart: () -> Unit) {
    val spacing = LocalSpacing.current
    val omerta = LocalOmertaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = omerta.divider.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = spacing.screenPadding, vertical = 12.dp),
    ) {
        PrimaryButton(
            text = if (canStart) stringResource(id = R.string.rule_start)
            else stringResource(id = R.string.rule_start_disabled, seatsShortBy),
            enabled = canStart,
            onClick = onStart,
        )
    }
}
