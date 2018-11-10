package io.mkg20001.arubanetworkslogin

import android.content.*
import android.net.ConnectivityManager
import android.util.Log

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v(LoginActivity.TAG, "Network change, running login routine")
        Utils.bindToWifi(context)
        CaptiveLoginTask.run(context)
    }
}