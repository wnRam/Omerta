package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

/**
 * Persisted host configuration. Uses [RoleCount] (a small Pair-equivalent) instead
 * of `Pair<Role, Int>` because kotlinx.serialization handles concrete classes more
 * predictably than the Pair contextual serializer, and because it survives schema
 * evolution better if we later need to add per-role metadata.
 */
@Serializable
data class Preset(
    val id: String,
    val name: String,
    val roles: List<RoleCount>,
    val phaseTimers: Map<GamePhase, Int?>,
    val hostSeesRoles: Boolean,
    val createdAt: Long,
)

@Serializable
data class RoleCount(
    val role: Role,
    val count: Int,
)
