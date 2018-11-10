package io.mkg20001.arubanetworkslogin

import android.content.*
import android.util.Log

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Utils.wifiConnected(Utils.getConn(context))) {
            Log.v(Utils.TAG, "Network change, running login routine")
            Utils.bindToWifi(context)
            CaptiveLoginTask.run(context)
        } else {
            Log.v(Utils.TAG, "Network change, but no wifi")
        }
    }
}