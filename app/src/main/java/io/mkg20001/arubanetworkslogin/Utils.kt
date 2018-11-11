package io.mkg20001.arubanetworkslogin

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import java.util.*

object Utils {
    /**
     * Tag showed in logs
     */
    val TAG = "ARUBA_LOGIN"

    fun getWifi(connectivity: ConnectivityManager): Network? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getWifiOreo(connectivity)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getWifiLollipop(connectivity)
        }

        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun getWifiLollipop(connectivity: ConnectivityManager): Network? {
        for (it in connectivity.allNetworks) {
            if (connectivity.getNetworkInfo(it).type == ConnectivityManager.TYPE_WIFI) {
                return it
            }
        }
        Log.e(TAG, "No wifi net found")
        return null
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getWifiOreo(connectivity: ConnectivityManager): Network? {
        return getWifiLollipop(connectivity)
        /* TODO: add

         	getType()

            This method was deprecated in API level 28.
            Callers should switch to checking NetworkCapabilities.hasTransport(int) instead with one of the NetworkCapabilities#TRANSPORT_* constants :
            getType() and getTypeName() cannot account for networks using multiple transports.
            Note that generally apps should not care about transport;
            NetworkCapabilities.NET_CAPABILITY_NOT_METERED and NetworkCapabilities.getLinkDownstreamBandwidthKbps() are calls
            that apps concerned with meteredness or bandwidth should be looking at, as they offer this information with much better accuracy.
         */
    }

    fun wifiConnected(connectivity: ConnectivityManager): Boolean {
        return getWifi(connectivity) != null || checkWifiBelow21(connectivity)
    }

    fun checkWifiBelow21(connectivity: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return connectivity.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
        return false
    }

    fun getConn(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun bindToWifiInner(context: Context): Boolean {
        val connectivity = getConn(context)
        val network = getWifi(connectivity)

        if (network != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return bindWifiNougat(connectivity, network)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return bindWifiLollipop(network)
            }
        }

        if (checkWifiBelow21(connectivity)) {
            return true
        }

        return false
    }

    fun bindToWifi(context: Context): Boolean {
        val res = bindToWifiInner(context)

        if (!res) {
            Log.e(TAG, "Aren't using wifi, this will fail")
            Toast.makeText(context.applicationContext, R.string.not_on_wifi,
                Toast.LENGTH_LONG).show()
        } else {
            Log.v(TAG, "Bound to wifi")
        }

        return res
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun bindWifiLollipop(network: Network): Boolean {
        return ConnectivityManager.setProcessDefaultNetwork(network)
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun bindWifiNougat(connectivity: ConnectivityManager, network: Network): Boolean {
        return connectivity.bindProcessToNetwork(network)
    }

}