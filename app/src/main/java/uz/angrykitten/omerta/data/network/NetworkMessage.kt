package uz.angrykitten.omerta.data.network

import kotlinx.serialization.Serializable
import uz.angrykitten.omerta.domain.model.GamePhase
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team

/**
 * Wire protocol between host and clients. Sealed class with kotlinx.serialization
 * polymorphic serialization. Every message is JSON for human-readability and
 * forward compatibility — small payloads in any case (rooms are <16 people).
 *
 * Versioning: when the protocol changes incompatibly, bump [PROTOCOL_VERSION]
 * and reject mismatched [Hello] messages.
 */
@Serializable
sealed class NetworkMessage {

    @Serializable
    data class Hello(
        val protocolVersion: Int,
        val playerName: String,
    ) : NetworkMessage()

    @Serializable
    data class HelloAck(
        val playerId: String,
        val roomCode: String,
    ) : NetworkMessage()

    @Serializable
    data class PlayerJoined(val playerId: String, val name: String) : NetworkMessage()

    @Serializable
    data class PlayerLeft(val playerId: String) : NetworkMessage()

    @Serializable
    data class GameStarted(val roleAssignments: Map<String, Role>) : NetworkMessage()

    @Serializable
    data class PhaseChanged(val phase: GamePhase, val durationSeconds: Int?, val round: Int) : NetworkMessage()

    @Serializable
    data class TimerUpdate(val secondsRemaining: Int) : NetworkMessage()

    @Serializable
    data object TimerPaused : NetworkMessage()

    @Serializable
    data object TimerResumed : NetworkMessage()

    @Serializable
    data class PlayerEliminated(val playerId: String) : NetworkMessage()

    @Serializable
    data class GameEnded(val winningTeam: Team?) : NetworkMessage()

    @Serializable
    data class HostMessage(val text: String) : NetworkMessage()

    /** Client-side authoritative ping. Hosts respond with [TimerUpdate] etc. */
    @Serializable
    data object Heartbeat : NetworkMessage()

    companion object {
        const val PROTOCOL_VERSION = 1
    }
}
