package io.mkg20001.arubanetworkslogin

import android.content.*
import android.net.ConnectivityManager
import android.util.Log

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMan.activeNetworkInfo
        if (netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI) {
            Log.v(LoginActivity.TAG, "Detected wifi connection, running login routine")
            CaptiveLoginTask.run(context.getSharedPreferences("UserInfo", 0))
        } else {
            Log.v(LoginActivity.TAG, "Network change, but no WiFi")
        }
    }
}