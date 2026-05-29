package uz.angrykitten.omerta.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.pow

/**
 * Client-side WebSocket. Connects to host on `ws://host:port/omerta` and
 * surfaces incoming [NetworkMessage]s on [incoming].
 *
 * Reconnection policy (per spec): up to 5 attempts with exponential backoff
 * (500ms, 1s, 2s, 4s, 8s). [connectionState] surfaces RECONNECTING so the
 * UI can show its banner.
 */
class OmertaClient {

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, FAILED }

    private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }

    private var sessionJob: Job? = null
    private var sendChannel: SendChannel? = null

    private val _incoming = MutableSharedFlow<NetworkMessage>(extraBufferCapacity = 64)
    val incoming: SharedFlow<NetworkMessage> = _incoming.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun connect(host: String, port: Int, hello: NetworkMessage.Hello) {
        disconnect() // idempotent
        sessionJob = scope.launch {
            var attempt = 0
            while (isActive && attempt <= MAX_RECONNECT_ATTEMPTS) {
                _connectionState.value = if (attempt == 0) ConnectionState.CONNECTING else ConnectionState.RECONNECTING
                val success = runCatching { runSession(host, port, hello) }
                if (success.isSuccess) {
                    attempt = 0
                    // session ended cleanly — close the loop
                    _connectionState.value = ConnectionState.DISCONNECTED
                    break
                } else {
                    Log.w(TAG, "Session failed: ${success.exceptionOrNull()?.message}")
                    attempt += 1
                    if (attempt > MAX_RECONNECT_ATTEMPTS) {
                        _connectionState.value = ConnectionState.FAILED
                        break
                    }
                    val backoffMs = (500L * 2.0.pow(attempt - 1).toLong()).coerceAtMost(8_000L)
                    delay(backoffMs)
                }
            }
        }
    }

    suspend fun send(message: NetworkMessage) {
        sendChannel?.send(message)
    }

    fun disconnect() {
        sessionJob?.cancel(); sessionJob = null
        sendChannel = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    /** Free all resources — call from VM onCleared(). */
    fun shutdown() {
        disconnect()
        scope.cancel()
        httpClient.close()
    }

    private suspend fun runSession(host: String, port: Int, hello: NetworkMessage.Hello) {
        httpClient.webSocket(request = { url("ws://$host:$port/omerta") }) {
            _connectionState.value = ConnectionState.CONNECTED
            sendChannel = object : SendChannel {
                override suspend fun send(msg: NetworkMessage) {
                    runCatching { this@webSocket.send(json.encodeToString<NetworkMessage>(msg)) }
                }
            }
            // Handshake first.
            send(json.encodeToString<NetworkMessage>(hello))
            try {
                for (frame in incoming) {
                    if (frame !is Frame.Text) continue
                    val text = frame.readText()
                    val msg = runCatching { json.decodeFromString<NetworkMessage>(text) }.getOrNull()
                    if (msg != null) _incoming.tryEmit(msg)
                }
            } finally {
                runCatching { close() }
                sendChannel = null
            }
        }
    }

    private fun interface SendChannel {
        suspend fun send(msg: NetworkMessage)
    }

    companion object {
        private const val TAG = "OmertaClient"
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }
}
