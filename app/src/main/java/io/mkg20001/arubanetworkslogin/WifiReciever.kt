package io.mkg20001.arubanetworkslogin

import android.content.*
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService

import android.net.ConnectivityManager
import android.net.NetworkInfo

import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMan.activeNetworkInfo
        if (netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI)
            Log.d("WifiReceiver", "Have Wifi Connection")
        else
            Log.d("WifiReceiver", "Don't have Wifi Connection")
    }
}