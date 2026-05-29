package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase { LOBBY, ROLE_REVEAL, NIGHT, DAY, VOTING, ENDED }
