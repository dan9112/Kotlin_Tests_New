package ru.kamaz.mapboxmap

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory

class ConnectionLiveData(context: Context) : LiveData<Boolean>() {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    override fun onInactive() {
        networkCallback?.let {
            cm.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        override fun onAvailable(network: Network) {
            Log.d("MapUtils", "onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability =
                networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d("MapUtils", "onAvailable: $network, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Log.d("MapUtils", "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            Log.d("MapUtils", "onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }

    }
}

object DoesNetworkHaveInternet {

    // Make sure to execute this on a background thread.
    fun execute(socketFactory: SocketFactory): Boolean {
        return try {
            Log.d("MapUtils", "PINGING google.")
            val socket =
                socketFactory.createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            Log.d("MapUtils", "PING success.")
            true
        } catch (e: IOException) {
            Log.e("MapUtils", "No internet connection. $e")
            false
        }
    }
}