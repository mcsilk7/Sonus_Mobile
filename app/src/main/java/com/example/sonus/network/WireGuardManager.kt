package com.example.sonus.network

import android.content.Context
import android.util.Log
import com.wireguard.android.backend.*
import com.wireguard.config.*
import com.wireguard.crypto.Key
import java.net.InetAddress

object WireGuardManager {

    private const val TAG = "SonusVPN"
    
    // --- KONFIGURACJA VPN ---
    private const val SERVER_PUBLIC_KEY = "3KUtMDNdJi2jFuastHWfZcORVBGGRUAff7TPJNnWOBw="
    private const val SERVER_ENDPOINT = "TWÓJ_PUBLICZNY_IP_SERWERA:51820" 
    private const val CLIENT_PRIVATE_KEY = "sFabw9kg8n6L2NRMlJCvz805GYBcFZt91QRmvIYBEU8="
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
            backend = GoBackend(context)
        }
    }

    suspend fun startVpn() {
        try {
            if (tunnel == null) tunnel = SonusTunnel()
            
            val config = Config.Builder()
                .setInterface(
                    Interface.Builder()
                        .addAddress(InetNetwork.parse("10.0.0.2/32"))
                        .setPrivateKey(Key.fromBase64(CLIENT_PRIVATE_KEY))
                        .build()
                )
                .addPeer(
                    Peer.Builder()
                        .addAllowedIP(InetNetwork.parse("10.0.0.0/24"))
                        .setEndpoint(InetEndpoint.parse(SERVER_ENDPOINT))
                        .setPublicKey(Key.fromBase64(SERVER_PUBLIC_KEY))
                        .build()
                )
                .build()

            backend?.setState(tunnel!!, Tunnel.State.UP, config)
            Log.i(TAG, "VPN Tunel podniesiony pomyślnie")
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas uruchamiania VPN", e)
        }
    }

    suspend fun stopVpn() {
        tunnel?.let {
            backend?.setState(it, Tunnel.State.DOWN, null)
        }
    }
}
