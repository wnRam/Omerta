package uz.angrykitten.omerta.domain.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Player
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team
import java.util.UUID

/**
 * Single-process holder for "the game we're currently running."
 *
 * Both Local and LAN modes write through this object — keeping it
 * process-wide (a singleton inside [uz.angrykitten.omerta.OmertaApp]) means
 * every screen pulls from the same source of truth via [state] and doesn't
 * need to thread game state through nav arguments.
 *
 * In LAN mode the host's GameSession is authoritative; clients mirror it
 * from network events. In Local mode there's only one device, so there's
 * only one GameSession.
 */
class GameSession {

    data class State(
        val mode: GameMode = GameMode.LOCAL,
        val roomCode: String = "",
        val players: List<Player> = emptyList(),
        val phase: GamePhase = GamePhase.LOBBY,
        val round: Int = 0,
        val secondsRemaining: Int? = null,
        val timerPaused: Boolean = false,
        val hostSeesRoles: Boolean = false,
        val allowSkipReveal: Boolean = true,
        val phaseTimers: Map<GamePhase, Int?> = RoomConfig.DEFAULT_PHASE_TIMERS,
        val revealCursor: Int = 0,
        val winningTeam: Team? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun startNewSession(config: RoomConfig, players: List<Player>) {
        val assigned = assignRoles(config, players)
        _state.value = State(
            mode = config.mode,
            roomCode = RoomCode.generate(),
            players = assigned,
            phase = GamePhase.ROLE_REVEAL,
            round = 1,
            hostSeesRoles = config.hostSeesRoles,
            allowSkipReveal = config.allowSkipReveal,
            phaseTimers = config.phaseTimersSec,
        )
    }

    /**
     * In Local mode players are anonymous "Seat 1..N" until the host enters
     * names. We pre-fill so the Rule Setup → Reveal flow can run without an
     * extra "name your players" screen.
     */
    fun seedAnonymousPlayers(count: Int): List<Player> =
        (1..count).map { i ->
            Player(
                id = UUID.randomUUID().toString(),
                name = "Seat $i",
                isHost = (i == 1),
            )
        }

    fun advanceReveal() {
        _state.update { s -> s.copy(revealCursor = (s.revealCursor + 1).coerceAtMost(s.players.size)) }
    }

    fun beginNight() {
        _state.update { s ->
            s.copy(
                phase = GamePhase.NIGHT,
                round = s.round.coerceAtLeast(1),
                secondsRemaining = s.phaseTimers[GamePhase.NIGHT],
                timerPaused = false,
            )
        }
    }

    fun advancePhase() {
        _state.update { s ->
            val (next, nextRound) = when (s.phase) {
                GamePhase.NIGHT -> GamePhase.DAY to s.round
                GamePhase.DAY -> GamePhase.VOTING to s.round
                GamePhase.VOTING -> GamePhase.NIGHT to (s.round + 1)
                GamePhase.LOBBY -> GamePhase.ROLE_REVEAL to 1
                GamePhase.ROLE_REVEAL -> GamePhase.NIGHT to 1
                GamePhase.ENDED -> GamePhase.ENDED to s.round
            }
            s.copy(
                phase = next,
                round = nextRound,
                secondsRemaining = s.phaseTimers[next],
                timerPaused = false,
            )
        }
    }

    fun extendTimer(seconds: Int) {
        _state.update { s ->
            val current = s.secondsRemaining ?: return@update s
            s.copy(secondsRemaining = current + seconds)
        }
    }

    fun tick() {
        _state.update { s ->
            if (s.timerPaused) return@update s
            val current = s.secondsRemaining ?: return@update s
            s.copy(secondsRemaining = (current - 1).coerceAtLeast(0))
        }
    }

    fun setPaused(paused: Boolean) {
        _state.update { it.copy(timerPaused = paused) }
    }

    fun toggleEliminated(playerId: String) {
        _state.update { s ->
            s.copy(players = s.players.map {
                if (it.id == playerId) it.copy(isEliminated = !it.isEliminated) else it
            })
        }
        checkWinCondition()
    }

    fun endGame(winningTeam: Team? = null) {
        val team = winningTeam ?: deriveWinningTeam()
        _state.update { it.copy(phase = GamePhase.ENDED, winningTeam = team) }
    }

    fun resetToLobby() {
        _state.value = State()
    }

    private fun assignRoles(config: RoomConfig, players: List<Player>): List<Player> {
        val roles = config.shuffledRoles().toMutableList()
        // FIXED: if there are more players than configured roles, pad with
        // Citizen so nobody receives a null assignment. Previously surplus
        // players got no role, breaking win-condition checks.
        val fallback = uz.angrykitten.omerta.domain.catalog.DefaultRoles.Citizen
        while (roles.size < players.size) {
            roles.add(fallback)
        }
        return players.mapIndexed { idx, p ->
            p.copy(assignedRole = roles[idx])
        }
    }

    private fun checkWinCondition() {
        val team = deriveWinningTeam() ?: return
        _state.update { it.copy(phase = GamePhase.ENDED, winningTeam = team) }
    }

    private fun deriveWinningTeam(): Team? {
        val alive = _state.value.players.filter { !it.isEliminated && it.assignedRole != null }
        if (alive.isEmpty()) return null
        val mafiaAlive = alive.count { it.assignedRole?.team == Team.MAFIA }
        val townAlive = alive.count { it.assignedRole?.team == Team.TOWN }
        val neutralAlive = alive.count { it.assignedRole?.team == Team.NEUTRAL }
        return when {
            mafiaAlive == 0 && neutralAlive == 0 -> Team.TOWN
            mafiaAlive >= townAlive + neutralAlive -> Team.MAFIA
            neutralAlive == 1 && mafiaAlive == 0 && townAlive == 0 -> Team.NEUTRAL
            else -> null
        }
    }
}
