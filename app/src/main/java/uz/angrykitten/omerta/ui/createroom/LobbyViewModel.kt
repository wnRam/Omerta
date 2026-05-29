package uz.angrykitten.omerta.ui.createroom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.angrykitten.omerta.domain.catalog.DefaultRoles
import uz.angrykitten.omerta.domain.game.GameSession
import uz.angrykitten.omerta.domain.game.RoomCode
import uz.angrykitten.omerta.domain.game.RoomConfig
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.localizedName
import java.util.UUID

/**
 * Glue between [CreateRoomViewModel]'s rules state and the running game.
 *
 * Local mode pre-fills "Seat N" players so the host can roll straight into
 * Role Reveal without an extra naming step.
 * LAN mode shows the host as Seat 1 and waits for guests to join (filled in
 * by Step 6 networking).
 */
class LobbyViewModel(
    private val gameSession: GameSession,
    private val createRoomState: CreateRoomViewModel.UiState,
    private val language: AppLanguage,
) : ViewModel() {

    data class UiState(
        val mode: GameMode,
        val roomCode: String,
        val players: List<Player>,
        val roleSummary: List<Pair<String, Int>>,
        val minPlayers: Int,
    )

    private val _state: MutableStateFlow<UiState>

    init {
        // FIXED: don't coerce seats above what the user configured — the
        // rule setup screen's canStart guard already ensures MIN_PLAYERS.
        // Coercing upward created phantom seats that received null roles.
        val seats = createRoomState.totalSeats
        val seedPlayers = if (createRoomState.mode == GameMode.LOCAL) {
            gameSession.seedAnonymousPlayers(seats)
        } else {
            listOf(
                Player(
                    id = UUID.randomUUID().toString(),
                    name = "Host",
                    isHost = true,
                ),
            )
        }
        _state = MutableStateFlow(
            UiState(
                mode = createRoomState.mode,
                roomCode = RoomCode.generate(),
                players = seedPlayers,
                roleSummary = buildRoleSummary(),
                minPlayers = RoomConfig.MIN_PLAYERS,
            ),
        )
    }

    val state: StateFlow<UiState> = _state.asStateFlow()

    fun kickPlayer(id: String) {
        _state.update { s -> s.copy(players = s.players.filterNot { it.id == id }) }
    }

    /** Called by LobbyScreen when host taps Start. Hands off to GameSession. */
    fun startGame() {
        val allRoles = DefaultRoles.all + createRoomState.customRoles
        val config = createRoomState.toConfig(allRoles)
        gameSession.startNewSession(config, _state.value.players)
    }

    private fun buildRoleSummary(): List<Pair<String, Int>> {
        val allRoles = (DefaultRoles.all + createRoomState.customRoles).associateBy { it.id }
        return createRoomState.roleCounts
            .mapNotNull { (id, count) ->
                if (count <= 0) return@mapNotNull null
                val role: Role = allRoles[id] ?: return@mapNotNull null
                role.localizedName(language) to count
            }
            .sortedByDescending { it.second }
    }
}
