package cz.covid19cz.erouska.receiver

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Network changes monitor
 */
class ConnectionStateMonitor(val onNetworkAvailable: () -> Unit) : NetworkCallback() {

    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    /**
     * Start tracking connectivity changes
     */
    fun enable(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    /**
     * Stop tracking connectivity changes
     */
    fun disable(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)
    }

    /**
     * Dispatch when network is available.
     */
    override fun onAvailable(network: Network) {
        onNetworkAvailable()
    }
}