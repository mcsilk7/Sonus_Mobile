package com.example.sonus.network

import android.content.Context
import android.util.Log
import com.wireguard.android.backend.*
import com.wireguard.config.*
import com.example.sonus.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object WireGuardManager {

    private const val TAG = "SonusVPN"
    
    // --- KONFIGURACJA VPN ---
    private val SERVER_PUBLIC_KEY = BuildConfig.WG_SERVER_PUBLIC_KEY
    private val SERVER_ENDPOINT = BuildConfig.WG_SERVER_ENDPOINT
    private val CLIENT_PRIVATE_KEY = BuildConfig.WG_CLIENT_PRIVATE_KEY
    private const val TUNNEL_NAME = "sonus_wg"
    // ------------------------

    private var backend: Backend? = null
    private var tunnel: SonusTunnel? = null

    class SonusTunnel : Tunnel {
        override fun getName() = TUNNEL_NAME
        override fun onStateChange(newState: Tunnel.State) {
            Log.d(TAG, "VPN State changed to: $newState")
        }
    }

    fun init(context: Context) {
        if (backend == null) {
            Log.d(TAG, "Initializing GoBackend...")
            backend = GoBackend(context.applicationContext)
        }
    }

    suspend fun startVpn() = withContext(Dispatchers.IO) {
        val context = com.example.sonus.SonusApp.appContext 
        Log.d(TAG, "startVpn() zacząłem na wątku: ${Thread.currentThread().name}")
        
        try {
            if (backend == null) {
                Log.d(TAG, "Tworzę GoBackend...")
                backend = GoBackend(context)
            }
            
            if (tunnel == null) tunnel = SonusTunnel()
            
            Log.d(TAG, "Buduję konfigurację...")
            val config = try {
                val iface = Interface.Builder()
                    .addAddress(InetNetwork.parse("10.0.0.2/32"))
                    .parsePrivateKey(CLIENT_PRIVATE_KEY)
                    .setMtu(1200)
                    .build()

                val peer = Peer.Builder()
                    .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
                    .setEndpoint(InetEndpoint.parse(SERVER_ENDPOINT))
                    .parsePublicKey(SERVER_PUBLIC_KEY)
                    .setPersistentKeepalive(25)
                    .build()

                Config.Builder()
                    .setInterface(iface)
                    .addPeer(peer)
                    .build()
            } catch (ce: Exception) {
                Log.e(TAG, "Błąd w parametrach konfiguracji: ${ce.message}", ce)
                throw Exception("Złe dane kluczy: ${ce.message}")
            }

            Log.d(TAG, "Wysyłam prośbę do systemu (setState)...")
            val result = backend?.setState(tunnel!!, Tunnel.State.UP, config)
            Log.i(TAG, "Wynik setState: $result")
            
            if (result == null) {
                Log.e(TAG, "setState zwrócił NULL - brak uprawnień lub błąd systemu")
                throw Exception("System Android nie pozwolił na start VPN.")
            }
            
            if (result == Tunnel.State.DOWN) {
                Log.w(TAG, "Tunel pozostał w stanie DOWN")
                throw Exception("VPN nie wystartował (DOWN).")
            }

            Log.i(TAG, "VPN Interfejs podniesiony (UP)!")
            
            // Start monitorowania, aby rozłączyć VPN po zabiciu apki
            val monitorIntent = android.content.Intent(context, VpnMonitorService::class.java)
            context.startService(monitorIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "KRYTYCZNY BŁĄD startVpn", e)
            throw e
        }
    }

    suspend fun stopVpn() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Zatrzymywanie VPN...")
        tunnel?.let {
            backend?.setState(it, Tunnel.State.DOWN, null)
        }
    }

    // Wersja do wywołania z usług bez blokowania korutyn
    fun stopVpnSync() {
        Log.d(TAG, "Zatrzymywanie VPN (Sync)...")
        try {
            tunnel?.let {
                backend?.setState(it, Tunnel.State.DOWN, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas synchronicznego stopowania VPN", e)
        }
    }
}
