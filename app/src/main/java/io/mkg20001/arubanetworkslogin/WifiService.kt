package io.mkg20001.arubanetworkslogin

import android.app.Service
import android.widget.Toast
import android.app.job.JobParameters
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.content.IntentFilter
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.app.job.JobService
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log


@RequiresApi(Build.VERSION_CODES.N)
class WifiService : JobService() {

    private var recv: WifiReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
    }


    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return Service.START_STICKY
    }


    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob")
        recv = WifiReceiver()
        registerReceiver(recv, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStopJob")
        unregisterReceiver(recv!!)
        return true
    }

    companion object {
        private val TAG = LoginActivity.TAG
    }
}