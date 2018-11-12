package io.mkg20001.arubanetworkslogin

import android.content.*
import android.util.Log
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Sentry.init("https://c8320a1568c24fb5be10fc429bc5e2a0@sentry.io/1321461", AndroidSentryClientFactory(context.applicationContext))
        if (Utils.wifiConnected(Utils.getConn(context))) {
            Log.v(Utils.TAG, "Network change, running login routine")
            Utils.bindToWifi(context)
            CaptiveLoginTask.run(context)
        } else {
            Log.v(Utils.TAG, "Network change, but no wifi")
        }
    }
}