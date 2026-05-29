package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val assignedRole: Role? = null,
    val isHost: Boolean = false,
    val isEliminated: Boolean = false,
    val isConnected: Boolean = true,
)
