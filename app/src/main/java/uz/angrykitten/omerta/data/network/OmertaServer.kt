package uz.angrykitten.omerta.data.network

import android.content.Context
import android.util.Log
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.net.ServerSocket
import java.util.Collections
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Ktor WebSocket server hosted on the host device. Listens on an OS-picked
 * port, advertised via [NsdHelper] under the room's chosen service name.
 *
 * Two communication directions:
 *   - Inbound: client → server messages emitted on [incoming] for the host VM to consume.
 *   - Outbound: [broadcast] sends a message to every connected client.
 *
 * Lifecycle: created with [start], torn down with [stop]. Re-starting is
 * idempotent — stop is called automatically if start is called again.
 */
class OmertaServer(
    private val context: Context,
    private val roomCode: String,
) {
    data class Endpoint(val port: Int, val serviceName: String)

    private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessions: MutableMap<String, DefaultWebSocketSession> =
        Collections.synchronizedMap(mutableMapOf())

    private val _incoming = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 64)
    val incoming: SharedFlow<ServerEvent> = _incoming.asSharedFlow()

    private var serverJob: Job? = null
    private var registration: NsdHelper.Registration? = null
    private var engine: io.ktor.server.engine.ApplicationEngine? = null

    sealed class ServerEvent {
        data class ClientConnected(val sessionId: String) : ServerEvent()
        data class ClientDisconnected(val sessionId: String) : ServerEvent()
        data class MessageReceived(val sessionId: String, val message: NetworkMessage) : ServerEvent()
    }

    fun start(serviceName: String = "Omerta-$roomCode"): Endpoint {
        stop() // idempotent
        val port = pickFreePort()
        val server = embeddedServer(CIO, host = "0.0.0.0", port = port) {
            install(WebSockets) {
                // AUDIT FIX: dropped contentConverter — we encode/decode JSON
                // manually via [json] in handleSession/broadcast/sendTo. Keeping
                // a converter we don't use forced an extra ktor-serialization
                // module on the classpath.
            }
            routing {
                webSocket("/omerta") { handleSession() }
            }
        }
        engine = server
        serverJob = scope.launch { runCatching { server.start(wait = true) } }
        val nsd = NsdHelper(context)
        registration = nsd.registerService(serviceName, port) { err ->
            Log.w(TAG, "NSD registration error: $err")
        }
        return Endpoint(port = port, serviceName = serviceName)
    }

    fun stop() {
        registration?.unregister(); registration = null
        engine?.stop(gracePeriodMillis = 200, timeoutMillis = 1000); engine = null
        serverJob?.cancel(); serverJob = null
        sessions.clear()
    }

    suspend fun broadcast(message: NetworkMessage) {
        val payload = json.encodeToString<NetworkMessage>(message)
        // Snapshot to avoid CME when sessions concurrently disconnect.
        val snapshot = synchronized(sessions) { sessions.values.toList() }
        snapshot.forEach { session ->
            runCatching { session.send(payload) }
        }
    }

    suspend fun sendTo(sessionId: String, message: NetworkMessage) {
        val session = sessions[sessionId] ?: return
        val payload = json.encodeToString<NetworkMessage>(message)
        runCatching { session.send(payload) }
    }

    private suspend fun DefaultWebSocketSession.handleSession() {
        val sessionId = UUID.randomUUID().toString()
        sessions[sessionId] = this
        _incoming.tryEmit(ServerEvent.ClientConnected(sessionId))
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val msg = runCatching { json.decodeFromString<NetworkMessage>(text) }.getOrNull()
                if (msg != null) {
                    _incoming.tryEmit(ServerEvent.MessageReceived(sessionId, msg))
                }
            }
        } catch (_: Throwable) {
            // Client disconnected mid-stream; cleanup below.
        } finally {
            sessions.remove(sessionId)
            _incoming.tryEmit(ServerEvent.ClientDisconnected(sessionId))
        }
    }

    // AUDIT FIX: bind a temporary ServerSocket(0) and read the OS-assigned
    // port, then close. Avoids the race of "pick a port, hope it's free"
    // and matches the spec's "random available port".
    private fun pickFreePort(): Int {
        ServerSocket(0).use { return it.localPort }
    }

    companion object {
        private const val TAG = "OmertaServer"
    }
}
