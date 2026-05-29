package uz.angrykitten.omerta.ui.createroom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.angrykitten.omerta.domain.catalog.DefaultRoles
import uz.angrykitten.omerta.domain.game.RoomConfig
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Role

/**
 * Backing state for the entire Create-Room flow: Rule Setup, Role Editor,
 * and Lobby all read/write here. Lives until the user backs out of the
 * Create-Room subgraph (single-activity scope, shared via `viewModel(...)`
 * keyed on a stable factory).
 */
class CreateRoomViewModel : ViewModel() {

    data class UiState(
        val mode: GameMode = GameMode.LOCAL,
        val roleCounts: Map<String, Int> = emptyMap(),
        val customRoles: List<Role> = emptyList(),
        val phaseTimers: Map<GamePhase, Int?> = RoomConfig.DEFAULT_PHASE_TIMERS,
        val hostSeesRoles: Boolean = false,
        val allowSkipReveal: Boolean = true,
    ) {
        val totalSeats: Int get() = roleCounts.values.sum()
        val canStart: Boolean get() = totalSeats >= RoomConfig.MIN_PLAYERS
        val seatsShortBy: Int
            get() = (RoomConfig.MIN_PLAYERS - totalSeats).coerceAtLeast(0)

        fun toConfig(allRoles: List<Role>): RoomConfig {
            val rolesById = allRoles.associateBy { it.id }
            return RoomConfig(
                mode = mode,
                roleCounts = roleCounts.mapNotNull { (id, count) ->
                    val role = rolesById[id] ?: return@mapNotNull null
                    if (count > 0) role to count else null
                },
                phaseTimersSec = phaseTimers,
                hostSeesRoles = hostSeesRoles,
                allowSkipReveal = allowSkipReveal,
            )
        }
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun setMode(mode: GameMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun adjustRole(roleId: String, delta: Int) {
        _state.update { s ->
            val current = s.roleCounts[roleId] ?: 0
            val next = (current + delta).coerceAtLeast(0)
            s.copy(roleCounts = s.roleCounts + (roleId to next))
        }
    }

    fun setPhaseTimer(phase: GamePhase, seconds: Int?) {
        _state.update { s -> s.copy(phaseTimers = s.phaseTimers + (phase to seconds)) }
    }

    fun setHostSeesRoles(enabled: Boolean) {
        _state.update { it.copy(hostSeesRoles = enabled) }
    }

    fun setAllowSkipReveal(enabled: Boolean) {
        _state.update { it.copy(allowSkipReveal = enabled) }
    }

    fun addCustomRole(role: Role) {
        _state.update { s -> s.copy(customRoles = s.customRoles + role) }
    }

    fun deleteCustomRole(roleId: String) {
        _state.update { s ->
            s.copy(
                customRoles = s.customRoles.filterNot { it.id == roleId },
                roleCounts = s.roleCounts - roleId,
            )
        }
    }

    /** Snapshot of the full role universe (built-ins + author's customs). */
    fun allKnownRoles(): List<Role> = DefaultRoles.all + _state.value.customRoles

    fun loadFromPreset(roleCounts: Map<String, Int>, timers: Map<GamePhase, Int?>, hostSees: Boolean) {
        _state.update {
            it.copy(
                roleCounts = roleCounts,
                phaseTimers = timers,
                hostSeesRoles = hostSees,
            )
        }
    }

    fun reset() {
        _state.value = UiState()
    }
}
