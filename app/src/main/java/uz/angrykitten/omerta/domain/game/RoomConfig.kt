package uz.angrykitten.omerta.domain.game

import uz.angrykitten.omerta.domain.model.GameMode
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Role

/**
 * Host-side configuration for a single game session. Distinct from [uz.angrykitten.omerta.domain.model.Room]
 * because Room represents the *running* state (with players, current phase),
 * while RoomConfig captures only the rules + setup choices.
 */
data class RoomConfig(
    val mode: GameMode,
    val roleCounts: List<Pair<Role, Int>>,
    val phaseTimersSec: Map<GamePhase, Int?>,
    val hostSeesRoles: Boolean,
    val allowSkipReveal: Boolean,
) {
    val totalSeats: Int get() = roleCounts.sumOf { it.second }

    /** Flat shuffled role list ready for assignment, one entry per seat. */
    fun shuffledRoles(): List<Role> =
        roleCounts.flatMap { (role, count) -> List(count) { role } }.shuffled()

    fun isPlayable(): Boolean = totalSeats >= MIN_PLAYERS

    companion object {
        const val MIN_PLAYERS = 3

        val DEFAULT_PHASE_TIMERS: Map<GamePhase, Int?> = mapOf(
            GamePhase.NIGHT to 60,
            GamePhase.DAY to 180,
            GamePhase.VOTING to 60,
        )

        val PHASE_TIMER_RANGES: Map<GamePhase, IntRange> = mapOf(
            GamePhase.NIGHT to 30..120,
            GamePhase.DAY to 60..300,
            GamePhase.VOTING to 30..120,
        )

        fun blank(mode: GameMode = GameMode.LOCAL): RoomConfig = RoomConfig(
            mode = mode,
            roleCounts = emptyList(),
            phaseTimersSec = DEFAULT_PHASE_TIMERS,
            hostSeesRoles = false,
            allowSkipReveal = true,
        )
    }
}
