package com.biodataai.app.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors device network connectivity and exposes it as a Flow<Boolean>.
 * True = network available; False = offline.
 * Uses NetworkCallback for real-time updates without polling.
 */
class AppConnectivityManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Check current state immediately
        val isCurrentlyOnline = connectivityManager.activeNetwork != null
        trySend(isCurrentlyOnline)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
