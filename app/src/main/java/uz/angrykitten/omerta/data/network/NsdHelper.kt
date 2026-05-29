package uz.angrykitten.omerta.data.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.InetAddress

/**
 * Thin wrapper around Android's [NsdManager] for `_omerta._tcp`. Two surfaces:
 *  - [registerService] for hosts (returns a closeable token).
 *  - [discoverServices] for clients (returns a Flow of discovered [DiscoveredRoom]s).
 *
 * NSD callbacks on Android are notoriously fiddly (no Lifecycle, multiple
 * stop-paths). Wrapping them in coroutine Flows means consumers don't have
 * to remember to unregister.
 */
class NsdHelper(context: Context) {

    private val manager: NsdManager =
        context.applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager

    data class DiscoveredRoom(
        val serviceName: String,
        val host: InetAddress,
        val port: Int,
    )

    interface Registration {
        fun unregister()
    }

    fun registerService(serviceName: String, port: Int, onError: (String) -> Unit = {}): Registration {
        val info = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "Registered: ${serviceInfo.serviceName}")
            }
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                onError("Registration failed: $errorCode")
            }
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) { /* no-op */ }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "Unregistration failed: $errorCode")
            }
        }
        manager.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
        return object : Registration {
            override fun unregister() {
                runCatching { manager.unregisterService(listener) }
            }
        }
    }

    fun discoverServices(): Flow<List<DiscoveredRoom>> = callbackFlow {
        val rooms = mutableMapOf<String, DiscoveredRoom>()
        val resolveListenerFor: (NsdServiceInfo) -> NsdManager.ResolveListener = { _ ->
            object : NsdManager.ResolveListener {
                override fun onServiceResolved(resolved: NsdServiceInfo) {
                    val host = resolved.host ?: return
                    val room = DiscoveredRoom(resolved.serviceName, host, resolved.port)
                    rooms[resolved.serviceName] = room
                    trySend(rooms.values.toList())
                }
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                }
            }
        }
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                close(IllegalStateException("Start discovery failed: $errorCode"))
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "Stop discovery failed: $errorCode")
            }
            override fun onDiscoveryStarted(serviceType: String) {
                Log.i(TAG, "Discovery started: $serviceType")
            }
            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                manager.resolveService(service, resolveListenerFor(service))
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                rooms.remove(service.serviceName)
                trySend(rooms.values.toList())
            }
        }
        manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        awaitClose {
            runCatching { manager.stopServiceDiscovery(discoveryListener) }
        }
    }

    companion object {
        // AUDIT FIX: spec specifies `_omerta._tcp` (NOT _mafiaroles._tcp).
        const val SERVICE_TYPE = "_omerta._tcp."
        private const val TAG = "OmertaNsd"
    }
}
