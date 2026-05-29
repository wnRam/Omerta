package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

/**
 * Phase timer is nullable: `null` = infinite (no countdown for this phase).
 */
@Serializable
data class Room(
    val code: String,
    val mode: GameMode,
    val players: List<Player>,
    val roles: List<Role>,
    val phase: GamePhase,
    val round: Int,
    val hostSeesRoles: Boolean,
    val phaseTimers: Map<GamePhase, Int?>,
)
